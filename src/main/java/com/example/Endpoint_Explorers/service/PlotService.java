package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.component.PlotFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PlotService {
    public List<String> generatePlots(
            Map<String, List<Double>> resultMetricsMap,
            List<String> metricsNames,
            String algorithmName,
            String problemName,
            Timestamp startDate,
            Timestamp endDate,
            List<Integer> iterations
    ) {
        List<String> fileNamePaths = new ArrayList<>();

        if (metricsNames.size() == 1 && metricsNames.get(0).equals("all")) {
            for (String key : resultMetricsMap.keySet()) {
                String plotPath = PlotFactory.createPlot(
                        key,
                        algorithmName,
                        problemName,
                        startDate,
                        endDate,
                        iterations,
                        resultMetricsMap.get(key)
                );
                fileNamePaths.add(plotPath);
            }
        } else if (metricsNames.size() != 1) {
            for (String key : metricsNames) {
                String plotPath = PlotFactory.createPlot(
                        key,
                        algorithmName,
                        problemName,
                        startDate,
                        endDate,
                        iterations,
                        resultMetricsMap.get(key)
                );
                fileNamePaths.add(plotPath);
            }
        } else if (!metricsNames.get(0).equals("none")) {
            String singleMetric = metricsNames.get(0);
            String plotPath = PlotFactory.createPlot(
                    singleMetric,
                    algorithmName,
                    problemName,
                    startDate,
                    endDate,
                    iterations,
                    resultMetricsMap.get(singleMetric)
            );
            fileNamePaths.add(plotPath);
        }
        return fileNamePaths;
    }
}
