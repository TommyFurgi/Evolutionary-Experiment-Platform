package com.example.Endpoint_Explorers.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RunExperimentRequest {
    @NotBlank(message = "Problem name cannot be blank")
    private String problemName;

    @NotBlank(message = "Algorithm cannot be blank")
    private String algorithm;

    @Size(min = 1, message = "At least one metric is required")
    private List<String> metrics;

    private List<Integer> evaluationNumbers;

    public RunExperimentRequest(String problemName, String algorithm, List<String> metrics, List<Integer> evaluationNumbers) {
        this.problemName = problemName;
        this.algorithm = algorithm;
        this.metrics = metrics;
        this.evaluationNumbers = evaluationNumbers;
    }

    @Override
    public String toString() {
        return "RunExperimentRequest{" +
                "problemName='" + problemName + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", metrics=" + metrics +
                ", evaluationNumbers=" + evaluationNumbers +
                '}';
    }
}
