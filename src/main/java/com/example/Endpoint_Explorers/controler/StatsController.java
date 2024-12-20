package com.example.Endpoint_Explorers.controler;

import com.example.Endpoint_Explorers.request.StatsRequest;
import com.example.Endpoint_Explorers.service.StatisticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatisticsService service;

    @PostMapping
    public List<Double> getStats(@RequestBody @Valid StatsRequest request) {
        service.getStatsTimeInterval(request);
        return new ArrayList<>();
    }
}
