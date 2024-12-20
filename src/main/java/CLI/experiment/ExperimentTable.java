package CLI.experiment;

import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Column;

import java.util.*;

public class ExperimentTable {
    private static final int ITERATION_COLUMN_WIDTH = 20;
    private static final int COLUMN_PADDING = 2;
    private static final int DEFAULT_METRIC_COLUMN_WIDTH = 15;
    private static final int EXTRA_WIDTH_FOR_METRIC_NAME = 5;

    public static void displayTable(Experiment experiment) {
        List<Metrics> metrics = experiment.metrics();
        Set<String> uniqueMetricNames = getUniqueMetricNames(metrics);
        Column[] columns = createColumns(uniqueMetricNames);

        CommandLine.Help.ColorScheme colorScheme = new CommandLine.Help.ColorScheme.Builder()
                .ansi(CommandLine.Help.Ansi.AUTO)
                .build();
        CommandLine.Help.TextTable table = CommandLine.Help.TextTable.forColumns(colorScheme, columns);

        String[] headers = createTableHeaders(uniqueMetricNames);
        table.addRowValues(headers);

        Map<Integer, List<Metrics>> iterationToMetricsMap = mapIterationsToMetrics(metrics);
        List<Integer> sortedIterations = new ArrayList<>(iterationToMetricsMap.keySet());
        Collections.sort(sortedIterations);

        addRowsToTable(table, sortedIterations, iterationToMetricsMap, uniqueMetricNames);

        System.out.println(table);
    }

    private static Set<String> getUniqueMetricNames(List<Metrics> metrics) {
        Set<String> uniqueNames = new LinkedHashSet<>();
        for (Metrics metric : metrics) {
            uniqueNames.add(metric.metricsName());
        }
        return uniqueNames;
    }

    private static Column[] createColumns(Set<String> uniqueMetricNames) {
        int numColumns = 1 + uniqueMetricNames.size();
        Column[] columns = new Column[numColumns];

        columns[0] = new Column(ITERATION_COLUMN_WIDTH, COLUMN_PADDING, Help.Column.Overflow.WRAP);
        int idx = 1;
        for (String name : uniqueMetricNames) {
            int width = Math.max(DEFAULT_METRIC_COLUMN_WIDTH, name.length() + EXTRA_WIDTH_FOR_METRIC_NAME);
            columns[idx++] = new Column(width, COLUMN_PADDING, Help.Column.Overflow.WRAP);
        }
        return columns;
    }

    private static String[] createTableHeaders(Set<String> uniqueMetricNames) {
        int numColumns = 1 + uniqueMetricNames.size();
        String[] headers = new String[numColumns];
        headers[0] = "Iteration Number";
        int idx = 1;
        for (String name : uniqueMetricNames) {
            headers[idx++] = name;
        }
        return headers;
    }

    private static Map<Integer, List<Metrics>> mapIterationsToMetrics(List<Metrics> metrics) {
        Map<Integer, List<Metrics>> iterationToMetricsMap = new HashMap<>();
        for (Metrics metric : metrics) {
            iterationToMetricsMap.computeIfAbsent(metric.iterationNumber(), k -> new ArrayList<>()).add(metric);
        }
        return iterationToMetricsMap;
    }

    private static void addRowsToTable(CommandLine.Help.TextTable table, List<Integer> sortedIterations,
                                       Map<Integer, List<Metrics>> iterationToMetricsMap,
                                       Set<String> uniqueMetricNames) {

        for (int iteration : sortedIterations) {
            String[] row = new String[1 + uniqueMetricNames.size()];
            row[0] = String.valueOf(iteration);

            List<Metrics> iterationMetrics = iterationToMetricsMap.get(iteration);
            List<String> metricValues = uniqueMetricNames.stream()
                    .map(metricName -> iterationMetrics.stream()
                            .filter(metric -> metric.metricsName().equals(metricName))
                            .map(metric -> String.valueOf(metric.value()))
                            .findFirst()
                            .orElse("-"))
                    .toList();

            System.arraycopy(metricValues.toArray(new String[0]), 0, row, 1, metricValues.size());
            table.addRowValues(row);
        }
    }
}