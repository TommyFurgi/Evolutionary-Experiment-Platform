package com.example.Endpoint_Explorers.controler;

import com.example.Endpoint_Explorers.component.ExperimentDtoMapper;
import com.example.Endpoint_Explorers.component.ExperimentValidator;
import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.ExperimentDto;
import com.example.Endpoint_Explorers.request.ManyDifferentExperimentRequest;
import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import com.example.Endpoint_Explorers.service.ExperimentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/experiments")
@RequiredArgsConstructor
public class ExperimentController {
    private final ExperimentService service;
    private final ExperimentDtoMapper dtoMapper;
    private final ExperimentValidator validator;

    @PostMapping()
    public ResponseEntity<String> runExperiment(@RequestBody @Valid RunExperimentRequest request) {
        try {
            System.out.println("Received experiment request: " + request);
            log.info("Received experiment request: {}", request);
            validator.validateExperimentRequest(request);
            int experimentId = service.runExperiment(request);
            return ResponseEntity.ok("Experiment started successfully, experimentID :  " + experimentId);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        }
    }

    @PostMapping("/many")
    public ResponseEntity<String> runExperiments(@RequestBody @Valid RunExperimentRequest request) {
        try {
            System.out.println("Received experiments request: " + request);
            log.info("Received experiments request: {}", request);
            validator.validateExperimentRequest(request);
            service.runExperiments(request);
            return ResponseEntity.ok("Experiments started successfully");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        }
    }
    @PostMapping("/manyDifferent")
    public ResponseEntity<String> runManyDifferentExperiments(@RequestBody @Valid ManyDifferentExperimentRequest request) {
        try {
            log.info("Received multi-experiments request: {}", request);
            validator.validateMultiExperimentRequest(request);
            service.runManyDifferentExperiments(request);
            return ResponseEntity.ok("Request accepted. Experiments running in background.");
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Experiment> getExperimentById(@PathVariable int id) {
        log.info("Getting experiment: {}", id);
        return service.getExperimentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ready")
    public ResponseEntity<List<ExperimentDto>> getDoneExperiments() {
        List<Experiment> experiments = service.getReadyExperiments();
        List<ExperimentDto> experimentDtos = dtoMapper.convertToDtoList(experiments);
        log.debug("Returning {} experiments marked as 'ready'.", experimentDtos.size());
        return ResponseEntity.ok(experimentDtos);
    }
}
