package com.example.Endpoint_Explorers.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvContentConverter {

    public static String buildCsvContent(
            Map<String, List<Double>> metricsMap
    ) {
        List<String> metricNames = new ArrayList<>(metricsMap.keySet());
        StringBuilder sb = new StringBuilder();
        sb.append("NFE");
        for (String metricName : metricNames) {
            sb.append(";").append(metricName);
        }
        sb.append("\n");
        int rowCount = 0;
        if (!metricNames.isEmpty()) {
            rowCount = metricsMap.get(metricNames.get(0)).size();
        }

        for (int i = 0; i < rowCount; i++) {
            int nfeValue = (i + 1) * 100;
            sb.append(nfeValue);

            for (String metricName : metricNames) {
                List<Double> values = metricsMap.get(metricName);
                Double value = (i < values.size()) ? values.get(i) : null;
                sb.append(";").append(value != null ? value : "");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
