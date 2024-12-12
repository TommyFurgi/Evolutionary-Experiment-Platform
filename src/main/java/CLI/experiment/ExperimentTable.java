package CLI.experiment;

import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Column;

import java.util.*;

public class ExperimentTable {
    public static void displayTable(Experiment experiment) {
        List<Metrics> metrics = experiment.getMetrics();
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
            uniqueNames.add(metric.getMetricsName());
        }
        return uniqueNames;
    }

    private static Column[] createColumns(Set<String> uniqueMetricNames) {
        int numColumns = 1 + uniqueMetricNames.size();
        Column[] columns = new Column[numColumns];

        columns[0] = new Column(20, 2, Help.Column.Overflow.WRAP);
        int idx = 1;
        for (String name : uniqueMetricNames) {
            int width = Math.max(15, name.length() + 5);
            columns[idx++] = new Column(width, 2, Help.Column.Overflow.WRAP);
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
            iterationToMetricsMap.computeIfAbsent(metric.getIterationNumber(), k -> new ArrayList<>()).add(metric);
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

            int idx = 1;
            for (String metricName : uniqueMetricNames) {
                boolean found = false;
                for (Metrics metric : iterationMetrics) {
                    if (metric.getMetricsName().equals(metricName)) {
                        row[idx++] = String.valueOf(metric.getValue());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    row[idx++] = "-";
                }
            }
            table.addRowValues(row);
        }
    }
}