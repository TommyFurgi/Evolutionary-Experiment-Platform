package CLI.config;

public class CliDefaults {
    private static final CliDefaults INSTANCE = new CliDefaults();

    public static final String DEFAULT_STATUS = "";
    public static final String DEFAULT_PROBLEM = "";
    public static final String DEFAULT_ALGORITHM = "";
    public static final String DEFAULT_METRIC_GENERAL = "all";
    public static final String DEFAULT_METRIC_NONE = "";
    public static final String DEFAULT_START_DATE = "2024-01-01_00:00:00";
    public static final String DEFAULT_END_DATE = "";
    public static final String DEFAULT_STATISTIC_TYPE = "median";
    public static final String DEFAULT_EVALUATION_NUMBER = "1000";
    public static final String DEFAULT_EXPERIMENT_ITERATION_NUMBER = "1";
    public static final String DEFAULT_PLOT_VALUE = "false";
    public static final String DEFAULT_METRIC_NAMES = "none";

    private CliDefaults() {
    }

    public static CliDefaults CliDefaults() {
        return INSTANCE;
    }
}
