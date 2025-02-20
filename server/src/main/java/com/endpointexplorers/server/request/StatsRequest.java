package com.endpointexplorers.server.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.util.List;

public record StatsRequest(
        @NotBlank(message = "Problem name cannot be blank") String problemName,
        @NotBlank(message = "Algorithm cannot be blank") String algorithm,
        @NotNull(message = "Start date/time cannot be null") String startDateTime,
        @NotNull(message = "End date/time cannot be null") String endDateTime,
        @NotBlank(message = "Stat type cannot be blank") String statType,
        @JsonProperty("isPlot") boolean isPlot,
        @JsonProperty("isCsv") boolean isCsv,
        List<String> metricsNamesToPlot,
        String groupName
) {}
