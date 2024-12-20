package com.example.Endpoint_Explorers.model;

import java.sql.Timestamp;

public record ExperimentDto(
        int id,
        String problemName,
        String algorithm,
        int numberOfEvaluation,
        StatusEnum status,
        Timestamp datatime
) {
}