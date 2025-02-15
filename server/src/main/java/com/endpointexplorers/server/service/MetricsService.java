package com.endpointexplorers.server.service;


import com.endpointexplorers.server.mapper.MetricsNameMapper;
import com.endpointexplorers.server.model.Experiment;
import com.endpointexplorers.server.model.Metrics;
import com.endpointexplorers.server.request.RunExperimentRequest;
import lombok.RequiredArgsConstructor;
import org.moeaframework.analysis.collector.Observation;
import org.moeaframework.analysis.collector.Observations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final PersistenceService persistenceService;

    public void saveMetrics(String metricsName, Experiment experiment, int evaluationNumber, float value) {
        Metrics metrics = Metrics.builder()
                .metricsName(metricsName.toLowerCase())
                .experiment(experiment)
                .iterationNumber(evaluationNumber)
                .value(value)
                .build();

        persistenceService.saveMetrics(metrics);
    }

    public Set<String> processMetricsNames(Observations result, RunExperimentRequest request) {
        Set<String> metricsNames;
        if (request.metrics().size() == 1 && request.metrics().getFirst().equals("all")) {
            metricsNames = result.keys();
            metricsNames.remove("Approximation Set");
            metricsNames.remove("Population");
        } else {
            metricsNames = request.metrics().stream()
                    .map(MetricsNameMapper::mapString)
                    .collect(Collectors.toSet());
        }
        return metricsNames;
    }

    public void saveAllMetrics(Observations result, Set<String> metricsNames, Experiment experiment) {
        for (Observation observation : result) {
            for (String metricsName : metricsNames) {
                float value = ((Number) observation.get(metricsName)).floatValue();
                saveMetrics(metricsName.replace(" ", ""), experiment, observation.getNFE(), value);
            }
        }
    }

    public String parseMetricsName(String metricsName) {
        return metricsName.replace("-", "").replace(" ", "");
    }

    public List<String> parseMetricsList(List<String> metricsNames) {
        return metricsNames.stream()
                .map(this::parseMetricsName)
                .map(String::toLowerCase)
                .toList();
    }
}
