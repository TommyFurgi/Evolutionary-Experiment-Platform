package CLI.config;

import lombok.Getter;

@Getter
public class CliConfig {
    @Getter
    private static final CliConfig instance = new CliConfig();
    private final String baseUrl = "http://localhost:8080/";
    private final String runExperimentUrl = "http://localhost:8080/experiments";
    private final String runExperimentsUrl = "http://localhost:8080/experiments/many";
    private final String getExperimentUrl = "http://localhost:8080/experiments/";
    private final String checkStatusUrl = "http://localhost:8080/experiments/ready";
    private final String experimentListUrl = "http://localhost:8080/experiment-list";
    private final String statsUrl = "http://localhost:8080/stats";

    private CliConfig() {
    }
}