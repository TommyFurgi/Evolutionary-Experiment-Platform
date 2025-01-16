package com.example.Endpoint_Explorers.component;

import com.example.Endpoint_Explorers.model.FileDetails;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

public class CsvContentConverter {

    public static FileDetails createCsvFile(
            Map<String, List<Double>> metricsMap,
            String problemName,
            String algorithm,
            String startDateTime,
            String endDateTime
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

        byte[] csvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        String base64Encoded = Base64.getEncoder().encodeToString(csvBytes);

        String safeStartDateTime = startDateTime.replace(":", "-");
        String safeEndDateTime = endDateTime.replace(":", "-");

        String fileName = "stats_"
                + problemName + "_" + algorithm + "_"
                + safeStartDateTime + "_" + safeEndDateTime
                + ".csv";

        return new FileDetails(fileName, base64Encoded);
    }
}
