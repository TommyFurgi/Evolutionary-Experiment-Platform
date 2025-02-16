package com.endpointexplorers.server.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RunMultipleExperiments(
    @Size(min = 1, message = "At least one problem is required") List<String> problems,
    @Size(min = 1, message = "At least one algorithm is required") List<String> algorithms,
    @Size(min = 1, message = "At least one metric is required") List<String> metrics,
    @Min(value = 1, message = "Evaluation number must be greater than 0") Integer evaluationNumber,
    @Min(value = 1, message = "Experiments number must be greater than 0") Integer experimentsNumber,
    String groupName
) {}
