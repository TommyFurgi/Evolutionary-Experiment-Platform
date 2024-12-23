package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.component.ExperimentObservableFactory;
import com.example.Endpoint_Explorers.component.ExperimentValidator;
import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.StatusEnum;
import com.example.Endpoint_Explorers.repository.ExperimentRepository;
import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.analysis.collector.Observations;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentService {
    private final ExperimentObservableFactory observableFactory;
    private final ExperimentRepository repository;
    private final MetricsService metricsService;
    private final ExperimentValidator validator;


    public List<Integer> runExperiments(RunExperimentRequest request) {
        int n = request.getExperimentIterationNumber();
        List<Integer> experimentIds = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            try {
                experimentIds.add(runExperiment(request));
            } catch (Exception e) {
                log.error("Something failed for one of experiments. List of created experiments without an error: {}", experimentIds);
                throw e;
            }
        }
        return experimentIds;
    }

    @Transactional
    public int runExperiment(RunExperimentRequest request) {
        validator.validateExperimentParams(
                request.getProblemName(),
                request.getAlgorithm(),
                request.getMetrics(),
                request.getEvaluationNumber(),
                request.getExperimentIterationNumber()
        );

        Experiment experiment = initializeExperiment(request);
        log.info("Running experiment with request: {}", request);

        try {
            observableFactory.createExperimentObservable(request)
                    .subscribeOn(Schedulers.computation())
                    .subscribe(
                            result -> handleSuccess(experiment, result, request),
                            throwable -> handleError(experiment, throwable)
                    );

            return experiment.getId();
        } catch (Exception e) {
            log.error("Transaction failed for experiment: {}", experiment.getId(), e);
            throw e;
        }
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

    private void handleError(Experiment experiment, Throwable throwable) {
        experiment.setStatus(StatusEnum.FAILED);
        Experiment savedExperiment = repository.save(experiment);
        log.error("Experiment with id {} failed: {}", savedExperiment.getId(), throwable.getMessage());
    }

    private Experiment initializeExperiment(RunExperimentRequest request) {
        Experiment experiment = Experiment.builder()
                .problemName(request.getProblemName().toLowerCase())
                .algorithm(request.getAlgorithm().toLowerCase())
                .numberOfEvaluation(request.getEvaluationNumber())
                .status(StatusEnum.IN_PROGRESS)
                .datetime(new Timestamp(System.currentTimeMillis()))
                .metricsList(new ArrayList<>())
                .build();

        Experiment savedExperiment = repository.save(experiment);
        log.info("Experiment with id {} saved successfully: {}", savedExperiment.getId(), experiment);
        return experiment;
    }

    public Optional<Experiment> getExperimentById(int id) {
        return repository.findById(id).map(experiment -> {
            if (experiment.getStatus() == StatusEnum.READY) {
                experiment.setStatus(StatusEnum.COMPLETED);
                return repository.save(experiment);
            }
            return experiment;
        });
    }

    public List<Experiment> getReadyExperiments() {
        List<Experiment> experiments = repository.findByStatus(StatusEnum.READY);

        for (Experiment experiment : experiments) {
            experiment.setStatus(StatusEnum.COMPLETED);
            repository.save(experiment);
        }

        return experiments;
    }

    public List<Experiment> getFilteredExperiments(List<String> statuses, List<String> problems, List<String> algorithms, List<String> metrics) {
        validator.validateListParams(statuses, problems, algorithms, metrics);

        Set<String> filteredStatuses = (statuses.isEmpty() || statuses.get(0).isEmpty()) ? null : new HashSet<>(statuses);
        Set<String> filteredProblems = convertListToLowerCaseSet(problems);
        Set<String> filteredAlgorithms = convertListToLowerCaseSet(algorithms);
        Set<String> filteredMetrics = convertListToLowerCaseSet(metrics);

        return repository.findFilteredExperiments(filteredStatuses, filteredProblems, filteredAlgorithms, filteredMetrics);
    }

    private Set<String> convertListToLowerCaseSet(List<String> list) {
        if (list == null || list.isEmpty() || list.get(0).isEmpty()) {
            return null;
        }
        return list.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}