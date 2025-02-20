package com.endpointexplorers.server.component;

import com.endpointexplorers.server.model.MetricTypeEnum;
import com.endpointexplorers.server.model.StatusEnum;
import com.endpointexplorers.server.request.RunMultipleExperimentsRequest;
import com.endpointexplorers.server.request.RunExperimentsRequest;
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

    public void validateRunMultipleExperimentsRequest(RunExperimentsRequest request) {
        validateProblemName(request.problemName());
        validateAlgorithm(request.algorithm());
        validateMetrics(request.metrics());
        validateEvaluationNumber(request.evaluationNumber());
        validateExperimentsNumber(request.experimentsNumber());
    }

    public void validateRunMultipleExperimentsRequest(RunMultipleExperimentsRequest request) {
        if (request.problems() == null || request.problems().isEmpty()) {
            throw new IllegalArgumentException("At least one problem is required.");
        }
        if (request.algorithms() == null || request.algorithms().isEmpty()) {
            throw new IllegalArgumentException("At least one algorithm is required.");
        }

        request.problems().forEach(this::validateProblemName);
        request.algorithms().forEach(this::validateAlgorithm);
        validateMetrics(request.metrics());
        validateEvaluationNumber(request.evaluationNumber());
        validateExperimentsNumber(request.experimentsNumber());
    }

    public void validateStatsParams(String problemName, String algorithm, Timestamp startDate, Timestamp endDate, List<String> metrics) {
        validateProblemName(problemName);
        validateAlgorithm(algorithm);
        validateDates(startDate, endDate);
        validateMetrics(metrics);
    }

    public void validateListParams(List<String> statuses, List<String> problems, List<String> algorithms, List<String> metrics) {
        statuses.forEach(this::validateStatus);
        problems.forEach(this::validateProblemName);
        algorithms.forEach(this::validateAlgorithm);
        if (metrics != null && !metrics.isEmpty())
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
        for (String metric : metrics) {
            if (MetricTypeEnum.fromString(metric).isEmpty()) {
                throw new IllegalArgumentException("Unknown metric specified: " + metric);
            }
        }    }

    private void validateNumber(Integer number, String fieldName) {
        if (number == null || number <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0.");
        }
    }

    private void validateDates(Timestamp startDate, Timestamp endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null.");
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

    private void validateEvaluationNumber(Integer evaluationsNumber) {
        if (evaluationsNumber == null || evaluationsNumber <= 0)
            throw new IllegalArgumentException("Evaluations number must be greater than 0.");
    }

    private void validateExperimentsNumber(Integer experimentsNumber) {
        if (experimentsNumber == null || experimentsNumber <= 0)
            throw new IllegalArgumentException("Experiments number number must be greater than 0.");
    }
}
