package com.example.Endpoint_Explorers.model;

public record ExperimentDto(
        int id,
        String problemName,
        String algorithm,
        int numberOfEvaluation,
        StatusEnum status
) {
}