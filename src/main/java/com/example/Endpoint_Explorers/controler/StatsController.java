package com.example.Endpoint_Explorers.controler;

import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

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
                               @RequestParam("statType") String statType) {

        try {
            return ResponseEntity.ok(service.getStatsTimeFromInterval(problemName, algorithm, start, end, statType));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        }
    }
}
