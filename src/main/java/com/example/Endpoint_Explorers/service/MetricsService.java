package com.example.Endpoint_Explorers.service;


import com.example.Endpoint_Explorers.mapper.MetricsNameMapper;
import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.Metrics;
import com.example.Endpoint_Explorers.repository.MetricsRepository;
import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import lombok.RequiredArgsConstructor;
import org.moeaframework.analysis.collector.Observation;
import org.moeaframework.analysis.collector.Observations;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MetricsRepository repository;

    public void saveMetrics(String metricsName, Experiment experiment, int evaluationNumber, float value) {
        Metrics metrics = Metrics.builder()
                .metricsName(metricsName.toLowerCase())
                .experiment(experiment)
                .iterationNumber(evaluationNumber)
                .value(value)
                .build();

        repository.save(metrics);
    }

    public Set<String> processMetricsNames(Observations result, RunExperimentRequest request) {
        Set<String> metricsNames;
        if (request.getMetrics().size() == 1 && request.getMetrics().getFirst().equals("all")) {
            metricsNames = result.keys();
            metricsNames.remove("Approximation Set");
            metricsNames.remove("Population");
        } else {
            metricsNames = request.getMetrics().stream()
                    .map(MetricsNameMapper::mapString)
                    .collect(Collectors.toSet());
        }
        return metricsNames;
    }

    public void saveAllMetrics(Observations result, Set<String> metricsNames, Experiment experiment) {
        for (Observation observation : result) {
            for (String metricsName : metricsNames) {
                float value = ((Number) observation.get(metricsName)).floatValue();
                saveMetrics(metricsName.replace(" ", "-"), experiment, observation.getNFE(), value);
            }
        }
    }
}
