package com.endpointexplorers.server.controler;

import com.endpointexplorers.server.component.ExperimentDtoMapper;
import com.endpointexplorers.server.model.Experiment;
import com.endpointexplorers.server.model.ExperimentDto;
import com.endpointexplorers.server.request.ExperimentListRequest;
import com.endpointexplorers.server.service.ExperimentService;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> getExperiments(@RequestBody @Valid ExperimentListRequest request) {
        try {
            log.info("Fetching experiments with filters: {}", request);

            List<Experiment> experiments = service.getFilteredExperiments(request);
            List<ExperimentDto> experimentDtos = dtoMapper.convertToDtoList(experiments);

            return ResponseEntity.ok(experimentDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        }
    }
}