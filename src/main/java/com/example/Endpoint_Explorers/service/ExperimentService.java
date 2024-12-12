package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.component.ExperimentObservableFactory;
import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.StatusEnum;
import com.example.Endpoint_Explorers.repository.ExperimentRepository;
import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.analysis.collector.Observations;
import org.springframework.stereotype.Service;

import java.util.*;


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

        Set<String> metricsNames = metricsService.processMetricsNames(result, request);

        log.debug("Result keys: {}", result.keys());
        log.debug("Metrics names: {}", metricsNames);

        metricsService.saveAllMetrics(result, metricsNames, experiment);
        experiment.setStatus(StatusEnum.READY);
        repository.save(experiment);
        result.display();
    }

    private void handleError(Experiment experiment, Throwable throwable, RunExperimentRequest request) {
        experiment.setStatus(StatusEnum.FAILED);
        Experiment savedExperiment = repository.save(experiment);
        log.error("Experiment with id {} failed: {}", savedExperiment.getId(), throwable.getMessage());
    }

    private Experiment initializeExperiment(RunExperimentRequest request) {
        Experiment experiment = Experiment.builder()
                .problemName(request.getProblemName())
                .algorithm(request.getAlgorithm())
                .numberOfEvaluation(request.getEvaluationNumber())
                .status(StatusEnum.IN_PROGRESS)
                .metricsList(new ArrayList<>())
                .build();

        Experiment savedExperiment = repository.save(experiment);
        log.info("Experiment with id {} saved successfully: {}", savedExperiment.getId(), experiment);
        return experiment;
    }

    public Optional<Experiment> getExperimentById(int id){
        return repository.findById(id).map(experiment -> {
            if (experiment.getStatus() == StatusEnum.READY) {
                experiment.setStatus(StatusEnum.COMPLETED);
                return repository.save(experiment);
            }
            return experiment;
        });
    }

    public List<Experiment> getReadyExperiments() {
        List<Experiment> experiments =  repository.findByStatus(StatusEnum.READY);

        for (Experiment experiment : experiments) {
            experiment.setStatus(StatusEnum.COMPLETED);
            repository.save(experiment);
        }

        return experiments;
    }

    public List<Experiment> getAllExperimentsWithStatus(String status){
        if (status == null || status.trim().isEmpty() || status.equalsIgnoreCase("all")) {
            return repository.findAll();
        }

        try {
            StatusEnum statusEnum = StatusEnum.valueOf(status.toUpperCase());
            return repository.findByStatus(statusEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }
}