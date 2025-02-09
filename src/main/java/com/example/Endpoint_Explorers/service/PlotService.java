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

        if (metricsNames.isEmpty() || metricsNames.get(0).equals("none"))
            return fileNamePaths;
        else if (metricsNames.get(0).equals("all"))
            metricsNames = new ArrayList<>(resultMetricsMap.keySet());

        for (String singleName : metricsNames) {
            String plotPath = PlotFactory.createPlot(
                    singleName,
                    algorithmName,
                    problemName,
                    startDate,
                    endDate,
                    iterations,
                    resultMetricsMap.get(singleName)
            );
            fileNamePaths.add(plotPath);
        }

        return fileNamePaths;
    }
}
