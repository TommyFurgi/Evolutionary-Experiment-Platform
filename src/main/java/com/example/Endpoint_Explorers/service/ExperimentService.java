package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.component.ExperimentObservableFactory;
import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.Metrics;
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

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentService {
    private final ExperimentObservableFactory observableFactory;
    private final ExperimentRepository repository;
    private final MetricsService metricsService;

    public int runExperiment(RunExperimentRequest request) {
        Experiment experiment = initializeExperiment(request);
        log.info("Running experiment with request: {}", request);

        observableFactory.createExperimentObservable(request)
                .subscribeOn(Schedulers.computation())
                .subscribe(
                        result -> handleSuccess(experiment, result, request),
                        throwable -> handleError(experiment, throwable, request)
                );
        return experiment.getId();
    }

    private void handleSuccess(Experiment experiment, Observations result, RunExperimentRequest request) {
        log.info("Experiment completed successfully for problem: {}", request.getProblemName());

        Set<String> metricsNames = processMetricsNames(result, request);

        log.debug("Result keys: {}", result.keys());
        log.debug("Metrics names: {}", metricsNames);

        saveMetrics(result, metricsNames, experiment);
        experiment.setStatus(StatusType.COMPLETED);
        repository.save(experiment);
        result.display();
    }

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

    private void saveMetrics(Observations result, Set<String> metricsNames, Experiment experiment) {
        for (Observation observation : result) {
            for (String metricsName : metricsNames) {
                float value = ((Number) observation.get(metricsName)).floatValue();
                metricsService.saveMetrics(metricsName, experiment, observation.getNFE(), value);
                log.debug("{} for step {}: {}", metricsName, observation.getNFE(), value);
            }
        }
    }

    private void handleError(Experiment experiment, Throwable throwable, RunExperimentRequest request) {
        experiment.setStatus(StatusType.FAILED);
        Experiment savedExperiment = repository.save(experiment);
        log.error("Experiment with id {} failed: {}", savedExperiment.getId(), throwable.getMessage());
    }

    private Experiment initializeExperiment(RunExperimentRequest request) {
        Experiment experiment = Experiment.builder()
                .problemName(request.getProblemName())
                .algorithm(request.getAlgorithm())
                .numberOfEvaluation(request.getEvaluationNumber())
                .status(StatusType.IN_PROGRESS)
                .metricsList(new ArrayList<>())
                .build();

        Experiment savedExperiment = repository.save(experiment);
        log.info("Experiment with id {} saved successfully: {}", savedExperiment.getId(), experiment);
        return experiment;
    }

    public Optional<Experiment> getExperimentById(int id){
        return repository.findById(id);
    }
}