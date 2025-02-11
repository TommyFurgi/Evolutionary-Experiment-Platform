package com.endpointexplorers.server.component;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CsvContentConverter {
    private static final int STEP_SIZE = 100;
    private static final String SEPARATOR = ",";

    public String buildCsvContent(Map<String, List<Double>> metricsMap) {
        if (metricsMap.isEmpty()) {
            return "";
        }

        List<String> metricNames = new ArrayList<>(metricsMap.keySet());
        StringBuilder csvBuilder = new StringBuilder();

        csvBuilder.append("NFE").append(SEPARATOR)
                .append(String.join(SEPARATOR, metricNames))
                .append("\n");

        int numberOfRows = metricsMap.get(metricNames.get(0)).size();

        for (int rowIndex = 0; rowIndex < numberOfRows; rowIndex++) {
            int nfeValue = (rowIndex + 1) * STEP_SIZE;
            List<String> rowValues = new ArrayList<>();
            rowValues.add(String.valueOf(nfeValue));

            for (String metricName : metricNames) {
                List<Double> values = metricsMap.get(metricName);
                Double value = (rowIndex < values.size()) ? values.get(rowIndex) : Double.NaN;
                rowValues.add(value.toString());
            }
            csvBuilder.append(String.join(SEPARATOR, rowValues)).append("\n");
        }
        return csvBuilder.toString();
    }
}
