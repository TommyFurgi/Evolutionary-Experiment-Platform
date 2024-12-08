package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.component.ExperimentObservableFactory;
import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.StatusType;
import com.example.Endpoint_Explorers.repository.ExperimentRepository;
import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mapper.MetricsNameMapper;
import org.moeaframework.analysis.collector.Observation;
import org.moeaframework.analysis.collector.Observations;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentService {
    private final ExperimentObservableFactory observableFactory;
    private final ExperimentRepository repository;
    private final MetricsService metricsService;

    /**
     * Starts the experiment and handles success and error scenarios.
     * @param request Experiment request data
     */
    public void runExperiment(RunExperimentRequest request) {
        log.info("Running experiment with request: {}", request);

        Observable<Observations> observable = observableFactory.createExperimentObservable(request);

        observable
                .subscribeOn(Schedulers.computation())
                .subscribe(
                        result -> handleSuccess(result, request),
                        throwable -> handleError(throwable, request)
                );
    }

    private void handleSuccess(Observations result, RunExperimentRequest request) {
        log.info("Experiment completed successfully for problem: {}", request.getProblemName());

        Set<String> metricsNames = processMetricsNames(result, request);

        log.debug("Result keys: {}", result.keys());
        log.debug("Metrics names: {}", metricsNames);

        saveExperimentResults(result, request, metricsNames);
        result.display();
    }

    /**
     * Processes the metrics names based on the experiment request or observation data.
     * @param result Observations produced from the experiment
     * @param request The original experiment request
     * @return A set of relevant metrics names
     */
    private Set<String> processMetricsNames(Observations result, RunExperimentRequest request) {
        Set<String> metricsNames;
        if (request.getMetrics().size() == 1 && request.getMetrics().getFirst().equals("all")) {
            metricsNames = result.keys();
            metricsNames.remove("Approximation Set");
            metricsNames.remove("Population");
        } else {
            metricsNames = request.getMetrics().stream()
                    .map(MetricsNameMapper::mapString)
                    .collect(Collectors.toSet());
        }
        return metricsNames;
    }

    private void saveExperimentResults(Observations result, RunExperimentRequest request, Set<String> metricsNames) {
        Experiment experiment = saveExperiment(request);
        saveMetrics(result, metricsNames, experiment);

    }

    private Experiment saveExperiment(RunExperimentRequest request) {
        Experiment experiment = Experiment.builder()
                .problemName(request.getProblemName())
                .algorithm(request.getAlgorithm())
                .numberOfEvaluation(request.getEvaluationNumber())
                .status(StatusType.COMPLETED)
                .build();
        Experiment savedExperiment = repository.save(experiment);
        log.info("Experiment with id {} saved successfully: {}", savedExperiment.getId(), experiment);
        return experiment;
    }

    /**
     * Saves observation data for each metric to the database.
     * @param result Observations data
     * @param metricsNames Set of metrics names
     * @param experiment Experiment entity associated with this data
     */
    private void saveMetrics(Observations result, Set<String> metricsNames, Experiment experiment) {
        for (Observation observation : result) {
            for (String metricsName : metricsNames) {
                float value = ((Number) observation.get(metricsName)).floatValue();
                metricsService.saveMetrics(metricsName, experiment, observation.getNFE(), value);

                log.debug("{} for step {}: {}", metricsName, observation.getNFE(), value);

            }
        }
    }

    private void handleError(Throwable throwable, RunExperimentRequest request) {
        Experiment experiment = Experiment.builder()
                .problemName(request.getProblemName())
                .algorithm(request.getAlgorithm())
                .numberOfEvaluation(request.getEvaluationNumber())
                .status(StatusType.FAILED)
                .build();
        Experiment savedExperiment = repository.save(experiment);
        log.error("Experiment with id {} failed: {}", savedExperiment.getId(), throwable.getMessage());
    }
}
