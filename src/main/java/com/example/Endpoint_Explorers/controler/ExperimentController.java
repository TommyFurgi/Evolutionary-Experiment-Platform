package com.example.Endpoint_Explorers.controler;

import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import com.example.Endpoint_Explorers.service.ExperimentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/experiment")
@RequiredArgsConstructor
public class ExperimentController {
    private final ExperimentService service;

    @PostMapping("/run")
    public ResponseEntity<String> runExperiment(@RequestBody @Valid RunExperimentRequest request) {
        System.out.println("Received experiment request: " + request);
        log.info("Received experiment request: {}", request);

        service.runExperiment(request); // Integer experimentId = service.runExperiment(request) -> i think we should pass in response ID of started experiment
        return ResponseEntity.ok("Experiment started successfully."); // So it should be "Experiment started successfully ID:  " + ID
    }
}
