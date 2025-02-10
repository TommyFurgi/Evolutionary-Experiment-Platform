package com.endpointexplorers.server.controler;

import com.endpointexplorers.server.component.ExperimentDtoMapper;
import com.endpointexplorers.server.component.ExperimentValidator;
import com.endpointexplorers.server.model.Experiment;
import com.endpointexplorers.server.model.ExperimentDto;
import com.endpointexplorers.server.request.GroupUpdateRequest;
import com.endpointexplorers.server.request.ManyDifferentExperimentRequest;
import com.endpointexplorers.server.request.RunExperimentRequest;
import com.endpointexplorers.server.service.ExperimentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

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

    @PutMapping("/group")
    public ResponseEntity<String> updateExperimentGroup(@RequestBody GroupUpdateRequest request) {
        try {
            List<Integer> updatedExperimentIds = service.updateGroupForExperiments(request.experimentIds(), request.newGroupName());
            log.info("Updated group for experiments: {}. New group name: {}", updatedExperimentIds, request.newGroupName());
            return ResponseEntity.ok(String.format("Group updated successfully for experiments: %s", updatedExperimentIds));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExperimentById(@PathVariable int id) {
        log.info("Deleting experiment with ID: {}", id);
        try {
            int deletedId = service.deleteExperimentById(id);
            return ResponseEntity.ok("Experiment with ID " + deletedId + " was deleted.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error while deleting experiment: " + e.getMessage());
        }
    }

    @DeleteMapping("/group/{groupName}")
    public ResponseEntity<String> deleteExperimentsByGroup(@PathVariable String groupName) {
        log.info("Deleting experiments for group: {}", groupName);
        try {
            int count = service.deleteExperimentsByGroup(groupName);
            if (count == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No experiments found for group '" + groupName + "'.");
            }
            return ResponseEntity.ok(count + " experiments deleted for group '" + groupName + "'");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error while deleting experiments by group: " + e.getMessage());
        }
    }

}
