package com.example.Endpoint_Explorers.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentDto {
    private int id;
    private String problemName;
    private String algorithm;
    private int numberOfEvaluation;
    private StatusEnum status;
}