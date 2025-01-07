package com.example.Endpoint_Explorers.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@NoArgsConstructor
@Getter
@Setter
public class ManyDifferentExperimentRequest {
    @Size(min = 1, message = "At least one problem is required")
    private List<String> problems;

    @Size(min = 1, message = "At least one algorithm is required")
    private List<String> algorithms;

    @Size(min = 1, message = "At least one metric is required")
    private List<String> metrics;

    @Min(value = 1, message = "Evaluation number must be greater than 0")
    private Integer evaluationNumber;

    @Min(value = 1, message = "Experiment iteration must be greater than 0")
    private Integer experimentIterationNumber;
}
