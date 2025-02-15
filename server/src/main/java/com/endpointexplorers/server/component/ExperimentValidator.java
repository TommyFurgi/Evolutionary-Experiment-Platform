package com.endpointexplorers.server.component;

import com.endpointexplorers.server.model.MetricTypeEnum;
import com.endpointexplorers.server.model.StatusEnum;
import com.endpointexplorers.server.request.ManyDifferentExperimentRequest;
import com.endpointexplorers.server.request.RunExperimentRequest;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.core.spi.AlgorithmFactory;
import org.moeaframework.core.spi.ProblemFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class ExperimentValidator {
    private final Set<String> allRegisteredProblems = ProblemFactory.getInstance().getAllRegisteredProblems();
    private final Set<String> allAlgorithms = AlgorithmFactory.getInstance().getAllDiagnosticToolAlgorithms();

    public void validateExperimentParams(String problemName, String algorithm, List<String> metrics, int evaluationNumber, int iterationNumber) {
        validateProblemName(problemName);
        validateAlgorithm(algorithm);
        validateMetrics(metrics);
        validateEvaluationNumber(evaluationNumber);
        validateIterationNumber(iterationNumber);
    }

    public void validateExperimentRequest(RunExperimentRequest request){
        this.validateExperimentParams(
                request.problemName(),
                request.algorithm(),
                request.metrics(),
                request.evaluationNumber(),
                request.experimentIterationNumber()
        );
    }

    public void validateMultiExperimentRequest(ManyDifferentExperimentRequest request) {
        if (request.problems() == null || request.problems().isEmpty()) {
            throw new IllegalArgumentException("At least one problem is required.");
        }
        request.problems().forEach(this::validateProblemName);
        if (request.algorithms() == null || request.algorithms().isEmpty()) {
            throw new IllegalArgumentException("At least one algorithm is required.");
        }
        request.algorithms().forEach(this::validateAlgorithm);

        if (request.metrics() == null || request.metrics().isEmpty()) {
            throw new IllegalArgumentException("At least one metric is required.");
        }
        validateMetrics(request.metrics());
        if (request.evaluationNumber() == null || request.evaluationNumber() <= 0) {
            throw new IllegalArgumentException("Evaluation number must be greater than 0.");
        }
        validateEvaluationNumber(request.evaluationNumber());
        if (request.experimentIterationNumber() == null || request.experimentIterationNumber() <= 0) {
            throw new IllegalArgumentException("Experiment iteration number must be greater than 0.");
        }
        validateIterationNumber(request.experimentIterationNumber());
    }


    public void validateStatsParams(String problemName, String algorithm, Timestamp startDate, Timestamp endDate, List<String> metrics) {
        validateProblemName(problemName);
        validateAlgorithm(algorithm);
        validateDates(startDate, endDate);
        if (!metrics.isEmpty() && !metrics.get(0).equals("none"))
            this.validateMetrics(metrics);
    }

    public void validateListParams(List<String> statuses, List<String> problemName, List<String> algorithm, List<String> metrics) {
        statuses.forEach(this::validateStatus);
        problemName.forEach(this::validateProblemName);
        algorithm.forEach(this::validateAlgorithm);
        if (!metrics.isEmpty())
            validateMetrics(metrics);
    }

        private void validateProblemName(String problemName) {
        if (!allRegisteredProblems.contains(problemName)) {
            throw new IllegalArgumentException("Problem not found: " + problemName);
        }
    }

    private void validateAlgorithm(String algorithm) {
        if (!allAlgorithms.contains(algorithm)) {
            throw new IllegalArgumentException("Algorithm not found: " + algorithm);
        }
    }

    private void validateMetrics(List<String> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            throw new IllegalArgumentException("Metrics list cannot be empty.");
        }

        for (String metricName : metrics) {
            if (MetricTypeEnum.fromString(metricName).isEmpty()) {
                throw new IllegalArgumentException("Unknown metric specified: " + metricName);
            }
        }
    }

    private void validateEvaluationNumber(int evaluationNumber) {
        if (evaluationNumber <= 0) {
            throw new IllegalArgumentException("Evaluation number must be greater than 0");
        }
    }

    private void validateIterationNumber(int iterationNumber) {
        if (iterationNumber <= 0) {
            throw new IllegalArgumentException("Iteration number must be greater than 0");
        }
    }

    private void validateDates(Timestamp startDate, Timestamp endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null.");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null.");
        }

        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
    }

    private void validateStatus(String experimentStatus) {
        try {
            StatusEnum.valueOf(experimentStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown status specified: " + experimentStatus);
        }
    }
}
