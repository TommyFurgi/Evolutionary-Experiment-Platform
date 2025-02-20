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
    public int deleteExperimentById(int experimentId) {
        metricsRepository.deleteByExperimentId(experimentId);
        return experimentRepository.deleteById(experimentId);
    }

    @Transactional
    public int deleteAllExperimentsByGroupName(String groupName) {
        List<Experiment> experiments = experimentRepository.findByGroupName(groupName);
        if (experiments.isEmpty()) {
            return 0;
        }

        List<Integer> experimentIds = experiments.stream()
                .map(Experiment::getId)
                .toList();

        metricsRepository.deleteByExperimentIds(experimentIds);
        return experimentRepository.deleteByGroupName(groupName);
    }

    @Transactional
    public Metrics saveMetrics(Metrics metrics) {
        return metricsRepository.save(metrics);
    }

    @Transactional
    public void updateExperimentsGroup(List<Integer> experimentIds, String groupName) {
        experimentRepository.updateGroupForExperiments(experimentIds, groupName);
    }
}
