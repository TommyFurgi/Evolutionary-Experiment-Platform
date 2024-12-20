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

    private Integer evaluationNumber;

    public RunExperimentRequest(String problemName, String algorithm, List<String> metrics, Integer evaluationNumber) {
        this.problemName = problemName;
        this.algorithm = algorithm;
        this.metrics = metrics;
        this.evaluationNumber = evaluationNumber;
    }
}
