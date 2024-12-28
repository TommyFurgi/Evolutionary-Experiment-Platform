package com.example.Endpoint_Explorers.controler;

import com.example.Endpoint_Explorers.component.ExperimentDtoMapper;
import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.ExperimentDto;
import com.example.Endpoint_Explorers.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/experiment-list")
@RequiredArgsConstructor
public class ExperimentListController {
    private final ExperimentService service;
    private final ExperimentDtoMapper dtoMapper;

    @PostMapping
    public ResponseEntity<?> getExperiments(@RequestBody Map<String, List<String>> bodyMap) {
        try {
            List<String> statuses = bodyMap.containsKey("statuses") ? bodyMap.get("statuses") : new ArrayList<>();
            List<String> problems = bodyMap.containsKey("problems") ? bodyMap.get("problems") : new ArrayList<>();
            List<String> algorithms = bodyMap.containsKey("algorithms") ? bodyMap.get("algorithms") : new ArrayList<>();
            List<String> metrics = bodyMap.containsKey("metrics") ? bodyMap.get("metrics") : new ArrayList<>();

            log.info("Fetching experiments with filters - Statuses: {}, Problems: {}, Algorithms: {}, Metrics: {}",
                    statuses, problems, algorithms, metrics);

            List<Experiment> experiments = service.getFilteredExperiments(statuses, problems, algorithms, metrics);
            List<ExperimentDto> experimentDtos = dtoMapper.convertToDtoList(experiments);

            return ResponseEntity.ok(experimentDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        }
    }
}