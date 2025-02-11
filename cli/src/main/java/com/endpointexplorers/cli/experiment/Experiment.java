package com.endpointexplorers.cli.experiment;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.util.List;

public record Experiment(
        @JsonProperty("id") int id,
        @JsonProperty("problemName") String problemName,
        @JsonProperty("algorithm") String algorithm,
        @JsonProperty("numberOfEvaluation") int numberOfEvaluation,
        @JsonProperty("status") String status,
        @JsonProperty("datetime") Timestamp datetime,
        @JsonProperty("metricsList") List<Metrics> metrics,
        @JsonProperty("groupName") String groupName
) {
    @Override
    public String toString() {
        return "Experiment " +
                "id=" + id +
                ", problemName='" + problemName + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", numberOfEvaluation=" + numberOfEvaluation +
                ", datetime=" + datetime +
                ", status='" + status +
                ", groupName='" + groupName + '\'';
    }
}
