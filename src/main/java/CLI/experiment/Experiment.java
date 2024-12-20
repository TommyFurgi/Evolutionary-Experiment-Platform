package CLI.experiment;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Experiment(
        @JsonProperty("id") int id,
        @JsonProperty("problemName") String problemName,
        @JsonProperty("algorithm") String algorithm,
        @JsonProperty("numberOfEvaluation") int numberOfEvaluation,
        @JsonProperty("status") String status,
        @JsonProperty("metricsList") List<Metrics> metrics
) {
    @Override
    public String toString() {
        return "Experiment " +
                "id=" + id +
                ", problemName='" + problemName + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", numberOfEvaluation=" + numberOfEvaluation +
                ", status='" + status + '\'';
    }
}
