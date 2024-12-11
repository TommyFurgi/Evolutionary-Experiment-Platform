package com.example.Endpoint_Explorers.controler;

import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.repository.ExperimentRepository;
import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import com.example.Endpoint_Explorers.service.ExperimentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/experiment")
@RequiredArgsConstructor
public class ExperimentController {
    private final ExperimentService service;
    private final ExperimentRepository experimentRepository;

    @PostMapping()
    public ResponseEntity<String> runExperiment(@RequestBody @Valid RunExperimentRequest request) {
        System.out.println("Received experiment request: " + request);
        log.info("Received experiment request: {}", request);

        int experimentId = service.runExperiment(request);
        return ResponseEntity.ok("Experiment started successfully, experimentID :  " + + experimentId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Experiment> getExperimentById(@PathVariable int id){
        log.info("Getting experiment: {}", id);
        Optional<Experiment> optionalExperiment = service.getExperimentById(id);
        return optionalExperiment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/ready")
    public ResponseEntity<List<Experiment>> getDoneExperiments() {
        List<Experiment> experiments = service.getReadyExperiments();
        log.info("Returning {} experiments marked as 'ready'.", experiments.size());
        return ResponseEntity.ok(experiments);
    }

    @GetMapping("/table")
    public ResponseEntity<List<Experiment>> getAllExperimentsTable() {
        List<Experiment> experiments = service.getAllExperiments();
        return ResponseEntity.ok(experiments);
    }
}
