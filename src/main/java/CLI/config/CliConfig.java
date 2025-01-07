package CLI.config;

public class CliConfig {
    private static final CliConfig INSTANCE = new CliConfig();

    public static final String BASE_URL = "http://localhost:8080/";
    public static final String RUN_EXPERIMENT_URL = "http://localhost:8080/experiments";
    public static final String RUN_EXPERIMENTS_URL = "http://localhost:8080/experiments/many";
    public static final String GET_EXPERIMENT_URL = "http://localhost:8080/experiments/";
    public static final String CHECK_STATUS_URL = "http://localhost:8080/experiments/ready";
    public static final String EXPERIMENT_LIST_URL = "http://localhost:8080/experiment-list";
    public static final String STATS_URL = "http://localhost:8080/stats";
    public static final String RUN_MANY_DIFFERENT_EXPERIMENTS_URL = "http://localhost:8080/experiments/manyDifferent";

    private CliConfig() {
    }

    public static CliConfig getInstance() {
        return INSTANCE;
    }
}