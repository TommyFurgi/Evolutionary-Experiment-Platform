package com.example.Endpoint_Explorers.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
public class StatsRequest {
    @NotBlank(message = "Problem name cannot be blank")
    private String problemName;

    @NotBlank(message = "Algorithm cannot be blank")
    private String algorithm;

    @NotNull(message = "Start date/time cannot be null")
    private String startDateTime;

    @NotNull(message = "End date/time cannot be null")
    private String endDateTime;

    @NotBlank(message = "Stat type cannot be blank")
    private String statType;

    @JsonProperty("isPlot")
    private boolean isPlot;

    @JsonProperty("isCsv")
    private boolean isCsv;

    private List<String> metricsNamesToPlot;

    private String groupName;
}
