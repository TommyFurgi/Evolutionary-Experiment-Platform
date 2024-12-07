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
import org.moeaframework.analysis.collector.Observation;
import org.moeaframework.analysis.collector.Observations;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static org.aspectj.runtime.internal.Conversions.floatValue;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentService {
    private final ExperimentObservableFactory observableFactory;
    private final ExperimentRepository repository;
    private final MetricsService metricsService;

    public void runExperiment(RunExperimentRequest request) {
        log.info("Running " + request.toString());

        Observable<Observations> observable = observableFactory.createExperimentObservable(request);

        observable
                .subscribeOn(Schedulers.computation())
                .subscribe(
                        result -> handleSuccess(result, request),
                        this::handleError
                );
    }

    private void handleSuccess(Observations result, RunExperimentRequest request) {
        log.info("Experiment completed successfully for problem: {}", request.getProblemName());

        saveExperimentResults(result, request);
//        Set<String> metricsNames = result.keys();
        Set<String> metricsNames = new HashSet<>();
        metricsNames.add("GenerationalDistance");
        System.out.println("Metrics names: " + metricsNames.toString());
        result.display();
        // here we should save results to repository and notify client instead of printing results
    }

    private void handleError(Throwable throwable) {
        log.error("Experiment execution failed: {}", throwable.getMessage());
    }

    private void saveExperimentResults(Observations result, RunExperimentRequest request) {
        Experiment experiment = Experiment.builder()
                .problemName(request.getProblemName())
                .algorithm(request.getAlgorithm())
                .numberOfEvaluation(request.getEvaluationNumber())
                .status(StatusType.COMPLETED)
                .build();
        repository.save(experiment);
        System.out.println("Experiment: " + experiment);

        Set<String> metricsNames = result.keys();
        for (Observation observation : result) {
            for (String metricsName : metricsNames) {
                if (metricsName.equals("Approximation Set") || metricsName.equals("Population")) {
                    continue;
                }
                System.out.println("Observation value: " + observation.get(metricsName) + ", Type: " + observation.get(metricsName).getClass().getName());
                float value = ((Number) observation.get(metricsName)).floatValue();
                metricsService.saveMetrics(metricsName, experiment, observation.getNFE(), value);
                System.out.println(metricsName + " for step: " + observation.getNFE()+ ": " + (value));
            }
        }
    }
}
