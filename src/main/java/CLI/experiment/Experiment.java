package CLI.experiment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class Experiment {
    @JsonProperty("id")
    private int id;

    @JsonProperty("problemName")
    private String problemName;

    @JsonProperty("algorithm")
    private String algorithm;

    @JsonProperty("numberOfEvaluation")
    private int numberOfEvaluation;

    @JsonProperty("status")
    private String status;

    @JsonProperty("metricsList")
    private List<Metrics> metrics;

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
