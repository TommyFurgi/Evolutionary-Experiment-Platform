package CLI.config;

import lombok.Getter;

public class CliConfig {
    @Getter
    private static final CliConfig instance = new CliConfig();

    @Getter
    private final String baseUrl = "http://localhost:8080/";

    @Getter
    private final String runExperimentUrl = "http://localhost:8080/experiment";

    @Getter
    private final String getExperimentUrl = "http://localhost:8080/experiment/";

    @Getter
    private final String getExperimentsTable = "http://localhost:8080/experiment/table";

    @Getter
    private final String checkStatusUrl = "http://localhost:8080/experiment/ready";

    private CliConfig() {}
}