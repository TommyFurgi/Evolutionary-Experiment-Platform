package com.endpointexplorers.server.model;

import java.sql.Timestamp;

public record ExperimentDto(
        int id,
        String problemName,
        String algorithm,
        int numberOfEvaluation,
        StatusEnum status,
        Timestamp datetime,
        String groupName
) {
}