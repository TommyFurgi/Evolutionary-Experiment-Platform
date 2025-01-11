package com.example.Endpoint_Explorers.controler;

import com.example.Endpoint_Explorers.component.FileContentConverter;
import com.example.Endpoint_Explorers.model.FileDetails;
import com.example.Endpoint_Explorers.model.MetricsAndFiles;
import com.example.Endpoint_Explorers.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatisticsService service;

    @GetMapping
    public ResponseEntity<?> getStats(@RequestParam("problemName") String problemName,
                                      @RequestParam("algorithm") String algorithm,
                                      @RequestParam("startDateTime") String start,
                                      @RequestParam("endDateTime") String end,
                                      @RequestParam("statType") String statType,
                                      @RequestParam("isPlot") String isPlot,
                                      @RequestParam(value = "metricsNamesToPlot", required = false) List<String> metricsNames) {

        try {
            if (Boolean.parseBoolean(isPlot)) {
                Map<String, List<Double>> metricsResults = service.getStatsTimeFromInterval(problemName, algorithm, start, end, statType, metricsNames);
                List<FileDetails> files = FileContentConverter.createFilesDetails(service.getFileNamePaths());

                return ResponseEntity.ok(new MetricsAndFiles(metricsResults, files));
            } else {
                Map<String, List<Double>> metricsResults = service.getStatsTimeFromInterval(
                        problemName,
                        algorithm,
                        start,
                        end,
                        statType,
                        new ArrayList<>(List.of("none")));

                return ResponseEntity.ok(new MetricsAndFiles(metricsResults, null));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }
}
