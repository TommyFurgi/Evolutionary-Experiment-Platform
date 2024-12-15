package CLI.experiment;


import com.fasterxml.jackson.annotation.JsonProperty;

public record Metrics(
        @JsonProperty("id") int id,
        @JsonProperty("metricsName") String metricsName,
        @JsonProperty("iterationNumber") int iterationNumber,
        @JsonProperty("value") float value
) {}