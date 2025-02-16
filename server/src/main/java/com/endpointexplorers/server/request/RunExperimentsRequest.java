package com.endpointexplorers.server.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RunExperimentRequest(
        @NotBlank(message = "Problem name cannot be blank") String problemName,
        @NotBlank(message = "Algorithm cannot be blank") String algorithm,
        @Size(min = 1, message = "At least one metric is required") List<String> metrics,
        @Min(value = 1, message = "Evaluation number must be greater than 0") Integer evaluationNumber,
        @Min(value = 1, message = "Experiment iteration must be greater than 0") Integer experimentIterationNumber,
        String groupName
) {}
