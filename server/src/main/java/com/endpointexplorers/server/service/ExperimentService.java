package com.endpointexplorers.server.service;

import com.endpointexplorers.server.component.ExperimentObservableFactory;
import com.endpointexplorers.server.component.ExperimentValidator;
import com.endpointexplorers.server.model.Experiment;
import com.endpointexplorers.server.model.StatusEnum;
import com.endpointexplorers.server.repository.ExperimentRepository;
import com.endpointexplorers.server.request.ExperimentListRequest;
import com.endpointexplorers.server.request.ManyDifferentExperimentRequest;
import com.endpointexplorers.server.request.RunExperimentRequest;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
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
    private  final PersistenceService experimentSaveService;


    public void runExperiments(RunExperimentRequest request) {
        Completable completable = Completable.fromCallable(() -> {
            List<Integer> experimentIds = new ArrayList<>();
            for (int i = 0; i < request.experimentIterationNumber(); i++) {
                try {
                    int expId = runExperiment(request);
                    experimentIds.add(expId);
                } catch (Exception e) {
                    log.error("Something failed for one of the experiments. List of created experiments without an error: {}", experimentIds, e);
                    throw e;
                }
            }
            return experimentIds;
        });

        completable
                .subscribeOn(Schedulers.computation())
                .doOnComplete(() -> log.info("All experiments completed successfully."))
                .doOnError(error -> log.error("Error occurred while running experiments: ", error))
                .subscribe();
    }

    public int runExperiment(RunExperimentRequest request) {
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

    public void runManyDifferentExperiments(ManyDifferentExperimentRequest request) {
        Completable completable = Completable.fromCallable(() -> {
            List<Integer> experimentIds = new ArrayList<>();
            for (String problem : request.problems()) {
                for (String algorithm : request.algorithms()) {
                    for (int i = 0; i < request.evaluationNumber(); i++) {
                        RunExperimentRequest singleReq = new RunExperimentRequest(
                                problem,
                                algorithm,
                                request.metrics(),
                                request.evaluationNumber(),
                                request.experimentIterationNumber(),
                                request.groupName()
                        );
                        int expId = runExperiment(singleReq);
                        experimentIds.add(expId);
                    }
                }
            }
            return experimentIds;
        });

        completable
                .subscribeOn(Schedulers.computation())
                .doOnComplete(() -> log.info("All experiments completed successfully."))
                .doOnError(error -> log.error("Error occurred while running experiments: ", error))
                .subscribe();
    }

    public void handleSuccess(Experiment experiment, Observations result, RunExperimentRequest request) {
        log.info("Experiment completed successfully for problem: {}", request.problemName());

        Set<String> metricsNames = metricsService.processMetricsNames(result, request);

        log.debug("Result keys: {}", result.keys());
        log.debug("Metrics names: {}", metricsNames);

        metricsService.saveAllMetrics(result, metricsNames, experiment);
        experiment.setStatus(StatusEnum.READY);

        experimentSaveService.saveExperiment(experiment);

        result.display();
    }

    public void handleError(Experiment experiment, Throwable throwable) {
        experiment.setStatus(StatusEnum.FAILED);
        Experiment savedExperiment = experimentSaveService.saveExperiment(experiment);
        log.error("Experiment with id {} failed: {}", savedExperiment.getId(), throwable.getMessage());
    }

    private Experiment initializeExperiment(RunExperimentRequest request) {
        String groupName = request.groupName().isEmpty() ? "none" : request.groupName();

        Experiment experiment = Experiment.builder()
                .problemName(request.problemName().toLowerCase())
                .algorithm(request.algorithm().toLowerCase())
                .numberOfEvaluation(request.evaluationNumber())
                .status(StatusEnum.IN_PROGRESS)
                .datetime(new Timestamp(System.currentTimeMillis()))
                .metricsList(new ArrayList<>())
                .groupName(groupName)
                .build();

        Experiment savedExperiment = experimentSaveService.saveExperiment(experiment);
        log.info("Experiment with id {} saved successfully: {}", savedExperiment.getId(), experiment);
        return savedExperiment;
    }

    public Optional<Experiment> getExperimentById(int id) {
        return repository.findById(id).map(experiment -> {
            if (experiment.getStatus() == StatusEnum.READY) {
                experiment.setStatus(StatusEnum.COMPLETED);
                return experimentSaveService.saveExperiment(experiment);
            }
            return experiment;
        });
    }

    public List<Experiment> getReadyExperiments() {
        List<Experiment> experiments = repository.findByStatus(StatusEnum.READY);

        for (Experiment experiment : experiments) {
            experiment.setStatus(StatusEnum.COMPLETED);
            experimentSaveService.saveExperiment(experiment);
        }

        return experiments;
    }

    public List<Experiment> getFilteredExperiments(ExperimentListRequest request) {
        List<String> problems = request.problems();
        List<String> algorithms = request.algorithms();
        List<String> metrics = request.metrics();
        List<String> statuses = request.statuses();
        List<String> groupNames = request.groupNames();

        validator.validateListParams(statuses, problems, algorithms, metrics);
        List<String> parsedMetrics = metrics.stream()
                .map(metricsService::parseMetricsName)
                .collect(Collectors.toList());

        Set<String> normalizedStatuses = normalizeList(statuses);
        Set<String> normalizedProblems = normalizeListToSet(problems);
        Set<String> normalizedAlgorithms = normalizeListToSet(algorithms);
        Set<String> normalizedMetrics = normalizeListToSet(parsedMetrics);
        Set<String> normalizedGroups = normalizeList(groupNames);

        return repository.findFilteredExperiments(
                normalizedStatuses,
                normalizedProblems,
                normalizedAlgorithms,
                normalizedMetrics,
                normalizedGroups
        );
    }

    private Set<String> normalizeList(List<String> list) {
        if (list == null || list.isEmpty() || list.get(0).isEmpty()) {
            return null;
        }
        return new HashSet<>(list);
    }

    private Set<String> normalizeListToSet(List<String> list) {
        if (list == null || list.isEmpty() || list.get(0).isEmpty()) {
            return null;
        }
        return list.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public List<Integer> updateGroupForExperiments(List<Integer> experimentIds, String newGroupName) {
        if (experimentIds == null || experimentIds.isEmpty()) {
            throw new IllegalArgumentException("Experiment IDs cannot be null or empty");
        }

        if (newGroupName == null) {
            throw new IllegalArgumentException("Group name cannot be null");
        }

        List<Experiment> experiments = repository.findAllById(experimentIds);
        String groupName = newGroupName.isEmpty() ? "none" : newGroupName;

        if (experiments.isEmpty()) {
            throw new IllegalArgumentException("No experiments found for the provided IDs");
        }

        List<Experiment> updatedExperiments = new ArrayList<>();
        for (Experiment experiment : experiments) {
            experiment.setGroupName(groupName);
            updatedExperiments.add(experiment);
        }

        experimentSaveService.saveAllExperiments(experiments);
        return updatedExperiments.stream()
                .map(Experiment::getId)
                .collect(Collectors.toList());
    }

    public int deleteExperimentById(int id) {
        Experiment experiment = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Experiment with id " + id + " not found"));
        experimentSaveService.deleteExperiment(experiment);
        return id;
    }

    public int deleteExperimentsByGroup(String groupName) {
        List<Experiment> experiments = repository.findByGroupName(groupName);
        int count = experiments.size();
        if (count > 0) {
            experimentSaveService.deleteAllExperiments(experiments);
        }
        return count;
    }
}