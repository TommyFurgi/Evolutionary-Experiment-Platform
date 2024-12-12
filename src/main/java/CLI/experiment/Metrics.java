package CLI.experiment;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Metrics {
    @JsonProperty("id")
    private int id;

    @JsonProperty("metricsName")
    private String metricsName;

    @JsonProperty("iterationNumber")
    private int iterationNumber;

    @JsonProperty("value")
    private float value;
}
