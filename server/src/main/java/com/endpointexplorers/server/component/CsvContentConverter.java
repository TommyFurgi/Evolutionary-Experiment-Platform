package com.endpointexplorers.server.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvContentConverter {
    private static final int STEP_SIZE = 100;

    public static String buildCsvContent(Map<String, List<Double>> metricsMap) {
        List<String> metricNames = new ArrayList<>(metricsMap.keySet());
        StringBuilder csvBuilder = new StringBuilder();

        csvBuilder.append("NFE");
        for (String metricName : metricNames) {
            csvBuilder.append(";").append(metricName);
        }
        csvBuilder.append("\n");

        int numberOfRows = metricNames.isEmpty() ? 0 : metricsMap.get(metricNames.get(0)).size();

        for (int rowIndex = 0; rowIndex < numberOfRows; rowIndex++) {
            int nfeValue = (rowIndex + 1) * STEP_SIZE;
            csvBuilder.append(nfeValue);

            for (String metricName : metricNames) {
                List<Double> values = metricsMap.get(metricName);
                Double value = (rowIndex < values.size()) ? values.get(rowIndex) : null;
                csvBuilder.append(";").append(value != null ? value : "");
            }
            csvBuilder.append("\n");
        }
        return csvBuilder.toString();
    }
}
