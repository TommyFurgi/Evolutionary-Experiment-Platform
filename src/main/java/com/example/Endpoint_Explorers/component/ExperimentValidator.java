package com.example.Endpoint_Explorers.component;

import com.example.Endpoint_Explorers.model.MetricTypeEnum;
import com.example.Endpoint_Explorers.model.StatusEnum;
import com.example.Endpoint_Explorers.request.ManyDifferentExperimentRequest;
import com.example.Endpoint_Explorers.request.RunExperimentRequest;
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
                request.getProblemName(),
                request.getAlgorithm(),
                request.getMetrics(),
                request.getEvaluationNumber(),
                request.getExperimentIterationNumber()
        );
    }

    public void validateMultiExperimentRequest(ManyDifferentExperimentRequest request) {
        if (request.getProblems() == null || request.getProblems().isEmpty()) {
            throw new IllegalArgumentException("At least one problem is required.");
        }
        request.getProblems().forEach(this::validateProblemName);
        if (request.getAlgorithms() == null || request.getAlgorithms().isEmpty()) {
            throw new IllegalArgumentException("At least one algorithm is required.");
        }
        request.getAlgorithms().forEach(this::validateAlgorithm);

        if (request.getMetrics() == null || request.getMetrics().isEmpty()) {
            throw new IllegalArgumentException("At least one metric is required.");
        }
        validateMetrics(request.getMetrics());
        if (request.getEvaluationNumber() == null || request.getEvaluationNumber() <= 0) {
            throw new IllegalArgumentException("Evaluation number must be greater than 0.");
        }
        validateEvaluationNumber(request.getEvaluationNumber());
        if (request.getExperimentIterationNumber() == null || request.getExperimentIterationNumber() <= 0) {
            throw new IllegalArgumentException("Experiment iteration number must be greater than 0.");
        }
        validateIterationNumber(request.getExperimentIterationNumber());
    }


    public void validateStatsParams(String problemName, String algorithm, Timestamp startDate, Timestamp endDate) {
        validateProblemName(problemName);
        validateAlgorithm(algorithm);
        validateDates(startDate, endDate);
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
