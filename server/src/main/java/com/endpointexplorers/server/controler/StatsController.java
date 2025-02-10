package com.endpointexplorers.server.controler;

import com.endpointexplorers.server.model.MetricsAndFiles;
import com.endpointexplorers.server.request.StatsRequest;
import com.endpointexplorers.server.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatisticsService statisticsService;

    @PostMapping
    public ResponseEntity<?> getStats(@RequestBody StatsRequest request) {
        System.out.println(request);
        try {
            MetricsAndFiles result = statisticsService.getStats(
                    request.getProblemName(),
                    request.getAlgorithm(),
                    request.getStartDateTime(),
                    request.getEndDateTime(),
                    request.getStatType(),
                    request.getMetricsNamesToPlot(),
                    request.getGroupName(),
                    request.isPlot(),
                    request.isCsv()
            );

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
