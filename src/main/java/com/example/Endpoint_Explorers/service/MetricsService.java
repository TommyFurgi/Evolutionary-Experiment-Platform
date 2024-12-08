package com.example.Endpoint_Explorers.service;


import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.Metrics;
import com.example.Endpoint_Explorers.repository.MetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MetricsRepository repository;

    public void saveMetrics(String metricsName, Experiment experiment, int evaluationNumber, float value) {
        Metrics metrics = Metrics.builder()
                .metricsName(metricsName)
                .experiment(experiment)
                .iterationNumber(evaluationNumber)
                .value(value)
                .build();

        repository.save(metrics);
    }

    public List<Metrics> getMetricsForExperiment(int experimentId) {
        return repository.findByExperimentId(experimentId);
    }
}
