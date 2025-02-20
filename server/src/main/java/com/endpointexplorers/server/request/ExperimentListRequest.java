package com.endpointexplorers.server.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ExperimentListRequest(
        @NotNull(message = "Problems list cannot be null") List<String> problems,
        @NotNull(message = "Algorithms list cannot be null") List<String> algorithms,
        @NotNull(message = "Metrics list cannot be null") List<String> metrics,
        @NotNull(message = "Statuses list cannot be null") List<String> statuses,
        @NotNull(message = "Group names list cannot be null") List<String> groupNames
) {}
