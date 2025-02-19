package com.endpointexplorers.server.service;

import com.endpointexplorers.server.component.ExperimentObservableFactory;
import com.endpointexplorers.server.component.ExperimentValidator;
import com.endpointexplorers.server.model.Experiment;
import com.endpointexplorers.server.model.StatusEnum;
import com.endpointexplorers.server.repository.ExperimentRepository;
import com.endpointexplorers.server.request.BaseRunExperimentRequest;
import com.endpointexplorers.server.request.ExperimentListRequest;
import com.endpointexplorers.server.request.RunMultipleExperimentsRequest;
import com.endpointexplorers.server.request.RunExperimentsRequest;
import io.reactivex.rxjava3.core.Observable;
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


    public List<Integer> runExperiments(BaseRunExperimentRequest request) {
        List<RunExperimentsRequest> requests;

        switch (request.getClass().getSimpleName()) {
            case "RunExperimentsRequest":
                RunExperimentsRequest runExperimentsRequest = (RunExperimentsRequest) request;
                validator.validateRunMultipleExperimentsRequest(runExperimentsRequest);
                requests = new ArrayList<>(List.of(runExperimentsRequest));
                break;
            case "RunMultipleExperimentsRequest":
                RunMultipleExperimentsRequest runMultipleExperimentsRequest = (RunMultipleExperimentsRequest) request;
                validator.validateRunMultipleExperimentsRequest(runMultipleExperimentsRequest);
                requests = generateExperiments(runMultipleExperimentsRequest);
                break;
            default:
                throw new IllegalArgumentException("Unsupported request type");
        }

        return runExperimentsInternal(requests);
    }

    private List<Integer> runExperimentsInternal(List<RunExperimentsRequest> requests) {
        List<Integer> experimentIds = new ArrayList<>();

        Observable<Integer> experimentObservable = Observable.fromIterable(requests)
                .flatMap(singleReq ->
                        Observable.range(0, singleReq.experimentsNumber())
                                .flatMap(i -> {
                                    Experiment experiment = initializeExperiment(singleReq);
                                    experimentIds.add(experiment.getId());

                                    return Observable.fromCallable(() -> {
                                        try {
                                            runSingleExperiment(singleReq, experiment);
                                            return experiment.getId();
                                        } catch (Exception e) {
                                            log.error("Experiment {} failed", experiment.getId(), e);
                                            throw e;
                                        }
                                    }).subscribeOn(Schedulers.io());
                                })
                );

        experimentObservable
                .observeOn(Schedulers.single())
                .subscribe(
                        id -> log.info("Experiment {} completed successfully.", id),
                        error -> log.error("Error occurred while running experiments", error),
                        () -> log.info("All experiments started successfully.")
                );

        return experimentIds;
    }

    private List<RunExperimentsRequest> generateExperiments(RunMultipleExperimentsRequest request) {
        return request.problems().stream()
                .flatMap(problem -> request.algorithms().stream()
                        .map(algorithm -> new RunExperimentsRequest(
                                problem, algorithm, request.metrics(),
                                request.evaluationNumber(), request.experimentsNumber(), request.groupName())))
                .collect(Collectors.toList());
    }

    public void runSingleExperiment(RunExperimentsRequest request, Experiment experiment) {
        observableFactory.createExperimentObservable(request)
                .subscribeOn(Schedulers.computation())
                .subscribe(
                        result -> processExperimentSuccess(experiment, result, request),
                        throwable -> processExperimentFailure(experiment, throwable)
                );
    }

    public void processExperimentSuccess(Experiment experiment, Observations result, RunExperimentsRequest request) {
        log.info("Experiment {} completed successfully.", experiment.getId());

        Set<String> metricsNames = metricsService.processMetricsNames(result, request);
        log.debug("Result keys: {}, Metrics names: {}", result.keys(), metricsNames);

        metricsService.saveAllMetrics(result, metricsNames, experiment);
        experiment.setStatus(StatusEnum.READY);
        experimentSaveService.saveExperiment(experiment);
    }

    public void processExperimentFailure(Experiment experiment, Throwable throwable) {
        experiment.setStatus(StatusEnum.FAILED);
        Experiment savedExperiment = experimentSaveService.saveExperiment(experiment);
        log.error("Experiment with id {} failed: {}", savedExperiment.getId(), throwable.getMessage());
    }

    private Experiment initializeExperiment(RunExperimentsRequest request) {
        Experiment experiment = Experiment.builder()
                .problemName(request.problemName().toLowerCase())
                .algorithm(request.algorithm().toLowerCase())
                .numberOfEvaluation(request.evaluationNumber())
                .status(StatusEnum.IN_PROGRESS)
                .datetime(new Timestamp(System.currentTimeMillis()))
                .metricsList(new ArrayList<>())
                .groupName(request.groupName().isEmpty() ? "none" : request.groupName())
                .build();

        return experimentSaveService.saveExperiment(experiment);
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

        String groupName = newGroupName.isEmpty() ? "none" : newGroupName;
        List<Experiment> experimentsInDb = repository.findAllById(experimentIds);
        if (experimentsInDb.isEmpty()) {
            throw new IllegalArgumentException("No experiments found for the provided IDs");
        }

        List<Integer> existingExperimentIds = experimentsInDb.stream()
                .map(Experiment::getId)
                .collect(Collectors.toList());

        experimentSaveService.updateExperimentsGroup(existingExperimentIds, groupName);

        return existingExperimentIds;
    }

    public int deleteExperimentById(int id) {
        int deleted = experimentSaveService.deleteExperimentById(id);
        if (deleted == 0) {
            throw new NoSuchElementException("Experiment with id " + id + " not found");
        }
        return id;
    }

    public int deleteExperimentsByGroup(String groupName) {
        return experimentSaveService.deleteAllExperimentsByGroupName(groupName);
    }
}