package com.endpointexplorers.server.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class RunExperimentRequest {
    @NotBlank(message = "Problem name cannot be blank")
    private String problemName;

    @NotBlank(message = "Algorithm cannot be blank")
    private String algorithm;

    @Size(min = 1, message = "At least one metric is required")
    private List<String> metrics;

    @Min(value = 1, message = "Evaluation number must be greater than 0")
    private Integer evaluationNumber;

    @Min(value = 1, message = "Experiment iteration must be greater than 0")
    private Integer experimentIterationNumber;

    private String groupName;

    public RunExperimentRequest(
            String problemName,
            String algorithm,
            List<String> metrics,
            Integer evaluationNumber,
            Integer experimentIterationNumber,
            String groupName) {
        this.problemName = problemName;
        this.algorithm = algorithm;
        this.metrics = metrics;
        this.evaluationNumber = evaluationNumber;
        this.experimentIterationNumber = experimentIterationNumber;
        this.groupName = groupName;
    }
}
