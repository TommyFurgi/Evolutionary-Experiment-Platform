package CLI.experiment;

import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Column;

import java.util.*;

public class ExperimentTable {
    public static void displayTable(Experiment experiment) {
        List<Experiment.Metric> metrics = experiment.getMetrics();
        Set<String> uniqueMetricNames = getUniqueMetricNames(metrics);
        Column[] columns = createColumns(uniqueMetricNames);

        CommandLine.Help.ColorScheme colorScheme = new CommandLine.Help.ColorScheme.Builder()
                .ansi(CommandLine.Help.Ansi.AUTO)
                .build();
        CommandLine.Help.TextTable table = CommandLine.Help.TextTable.forColumns(colorScheme, columns);

        String[] headers = createTableHeaders(uniqueMetricNames);
        table.addRowValues(headers);

        Map<Integer, List<Experiment.Metric>> iterationToMetricsMap = mapIterationsToMetrics(metrics);
        List<Integer> sortedIterations = new ArrayList<>(iterationToMetricsMap.keySet());
        Collections.sort(sortedIterations);

        addRowsToTable(table, sortedIterations, iterationToMetricsMap, uniqueMetricNames);

        System.out.println(table.toString());
    }

    private static Set<String> getUniqueMetricNames(List<Experiment.Metric> metrics) {
        Set<String> uniqueNames = new LinkedHashSet<>();
        for (Experiment.Metric metric : metrics) {
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

    private static Map<Integer, List<Experiment.Metric>> mapIterationsToMetrics(List<Experiment.Metric> metrics) {
        Map<Integer, List<Experiment.Metric>> iterationToMetricsMap = new HashMap<>();
        for (Experiment.Metric metric : metrics) {
            iterationToMetricsMap.computeIfAbsent(metric.getIterationNumber(), k -> new ArrayList<>()).add(metric);
        }
        return iterationToMetricsMap;
    }

    private static void addRowsToTable(CommandLine.Help.TextTable table, List<Integer> sortedIterations,
                                       Map<Integer, List<Experiment.Metric>> iterationToMetricsMap,
                                       Set<String> uniqueMetricNames) {
        for (int iteration : sortedIterations) {
            String[] row = new String[1 + uniqueMetricNames.size()];
            row[0] = String.valueOf(iteration);

            List<Experiment.Metric> iterationMetrics = iterationToMetricsMap.get(iteration);

            int idx = 1;
            for (String metricName : uniqueMetricNames) {
                boolean found = false;
                for (Experiment.Metric metric : iterationMetrics) {
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