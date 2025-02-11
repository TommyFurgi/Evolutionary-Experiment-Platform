package com.endpointexplorers.server.service;

import com.endpointexplorers.server.component.PlotFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlotService {
    private final PlotFactory plotFactory;

    public List<String> generatePlots(
            Map<String, List<Double>> resultMetricsMap,
            List<String> metricsNames,
            String algorithmName,
            String problemName,
            List<Integer> iterations
    ) {
        List<String> fileNamePaths = new ArrayList<>();

        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String folderName = currentDate.format(formatter);

        if (metricsNames.isEmpty() || metricsNames.get(0).equals("none"))
            return fileNamePaths;
        else if (metricsNames.get(0).equals("all"))
            metricsNames = new ArrayList<>(resultMetricsMap.keySet());

        for (String singleName : metricsNames) {
            String plotPath = plotFactory.createPlot(
                    folderName,
                    singleName,
                    algorithmName,
                    problemName,
                    iterations,
                    resultMetricsMap.get(singleName)
            );
            fileNamePaths.add(plotPath);
        }

        return fileNamePaths;
    }
}
