package com.example.Endpoint_Explorers.controler;

import com.example.Endpoint_Explorers.component.CsvContentConverter;
import com.example.Endpoint_Explorers.component.FileContentConverter;
import com.example.Endpoint_Explorers.model.FileDetails;
import com.example.Endpoint_Explorers.model.MetricsAndFiles;
import com.example.Endpoint_Explorers.service.CsvService;
import com.example.Endpoint_Explorers.service.PlotService;
import com.example.Endpoint_Explorers.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatisticsService statisticsService;

    @GetMapping
    public ResponseEntity<?> getStats(@RequestParam("problemName") String problemName,
                                      @RequestParam("algorithm") String algorithm,
                                      @RequestParam("startDateTime") String start,
                                      @RequestParam("endDateTime") String end,
                                      @RequestParam("statType") String statType,
                                      @RequestParam("isPlot") String isPlot,
                                      @RequestParam("isCsv") String isCsv,
                                      @RequestParam(value = "metricsNamesToPlot", required = false) List<String> metricsNames,
                                      @RequestParam("groupName") String groupName) {

        try {
            // Konwersja isPlot, isCsv na boolean
            boolean plot = Boolean.parseBoolean(isPlot);
            boolean csv  = Boolean.parseBoolean(isCsv);

            // Wywołujemy JEDNĄ metodę w serwisie, która zrobi wszystko.
            MetricsAndFiles result = statisticsService.getStats(
                    problemName,
                    algorithm,
                    start,
                    end,
                    statType,
                    metricsNames,
                    groupName,
                    plot,
                    csv
            );
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }
}
