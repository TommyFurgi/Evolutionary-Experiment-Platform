package com.endpointexplorers.cli.experiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ExperimentMapper {
    public static Experiment parseExperiment(ResponseEntity<String> response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response.getBody(), Experiment.class);
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing experiment data: " + e.getMessage());
        }
    }

    public static List<Experiment> parseExperimentList(ResponseEntity<String> response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response.getBody(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Experiment.class));
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing experiment list: " + e.getMessage());
        }
    }
}