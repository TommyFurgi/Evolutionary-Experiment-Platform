package com.example.Endpoint_Explorers.controler;

import com.example.Endpoint_Explorers.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatisticsService service;

    @GetMapping
    public Map<String, List<Double>> getStats(@RequestParam("problemName") String problemName,
                                              @RequestParam("algorithm") String algorithm,
                                              @RequestParam("startDateTime") String start,
                                              @RequestParam("endDateTime") String end) {
        return service.getStatsTimeFromInterval(problemName, algorithm, start, end);
    }
}
