package com.endpointexplorers.server.service;

import com.endpointexplorers.server.model.Experiment;
import com.endpointexplorers.server.model.Metrics;
import com.endpointexplorers.server.repository.ExperimentRepository;
import com.endpointexplorers.server.repository.MetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersistenceService {
    private final ExperimentRepository experimentRepository;
    private final MetricsRepository metricsRepository;

    @Transactional
    public Experiment saveExperiment(Experiment experiment) {
        return experimentRepository.save(experiment);
    }

    @Transactional
    public void saveAllExperiments(List<Experiment> experiments) {
        experimentRepository.saveAll(experiments);
    }

    @Transactional
    public void deleteExperiment(Experiment experiment) {
        experimentRepository.delete(experiment);
    }

    @Transactional
    public void deleteAllExperiments(List<Experiment> experiments) {
        experimentRepository.deleteAll(experiments);
    }

    @Transactional
    public Metrics saveMetrics(Metrics metrics) {
        return metricsRepository.save(metrics);
    }
}
