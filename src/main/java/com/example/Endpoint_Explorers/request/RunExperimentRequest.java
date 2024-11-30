package com.example.Endpoint_Explorers.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RunExperimentRequest {
    @NotBlank(message = "Problem name cannot be blank")
    private String problemName;

    @NotBlank(message = "Algorithm cannot be blank")
    private String algorithm;
}