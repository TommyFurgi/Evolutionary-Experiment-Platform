package com.example.Endpoint_Explorers.controler;

import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import com.example.Endpoint_Explorers.service.ExperimentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/experiment")
@RequiredArgsConstructor
public class ExperimentController {
    private final ExperimentService service;

    @PostMapping("/run")
    public ResponseEntity<String> runExperiment(@RequestBody @Valid RunExperimentRequest request) {
        log.info("Starting experiment: {}", request);
        service.runExperiment(request);
        return ResponseEntity.ok("Experiment started successfully.");
    }
}
