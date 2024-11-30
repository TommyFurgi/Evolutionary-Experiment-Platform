package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.repository.ExperimentRepository;
import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentService {
    private final ExperimentRepository repository;

    public void runExperiment(RunExperimentRequest experiment) {
        log.info("Running Experiment");
    }
}
