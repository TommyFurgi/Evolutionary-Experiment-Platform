package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.component.ExperimentObservableFactory;
import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.MetricTypeEnum;
import com.example.Endpoint_Explorers.model.StatusEnum;
import com.example.Endpoint_Explorers.repository.ExperimentRepository;
import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.analysis.collector.Observations;
import org.moeaframework.core.spi.AlgorithmFactory;
import org.moeaframework.core.spi.ProblemFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentService {
    private final ExperimentObservableFactory observableFactory;
    private final ExperimentRepository repository;
    private final MetricsService metricsService;
    private final Set<String> allRegisteredProblems = ProblemFactory.getInstance().getAllRegisteredProblems();
    private final Set<String> allAlgorithms = AlgorithmFactory.getInstance().getAllDiagnosticToolAlgorithms();


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
        validateRequest(request);

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

    private void validateRequest(RunExperimentRequest request) {
        if (!allRegisteredProblems.contains(request.getProblemName())) {
            throw new IllegalArgumentException("Problem not found: " + request.getProblemName());
        }
        if (!allAlgorithms.contains(request.getAlgorithm())) {
            throw new IllegalArgumentException("Algorithm not found: " + request.getAlgorithm());
        }

        for (String metricName : request.getMetrics()) {
            if (MetricTypeEnum.fromString(metricName).isEmpty()) {
                throw new IllegalArgumentException("Unknown metric specified: " + metricName);
            }
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
                .problemName(request.getProblemName())
                .algorithm(request.getAlgorithm())
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

    public List<Experiment> getAllExperimentsWithStatus(String status) {
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