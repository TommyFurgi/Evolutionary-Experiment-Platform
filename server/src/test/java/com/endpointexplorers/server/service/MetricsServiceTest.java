package com.endpointexplorers.server.service;

import static org.junit.jupiter.api.Assertions.*;
import com.endpointexplorers.server.mapper.MetricsNameMapper;
import com.endpointexplorers.server.model.Experiment;
import com.endpointexplorers.server.model.Metrics;
import com.endpointexplorers.server.repository.MetricsRepository;
import com.endpointexplorers.server.request.RunExperimentsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moeaframework.analysis.collector.Observation;
import org.moeaframework.analysis.collector.Observations;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {
    @Mock
    private MetricsRepository metricsRepository;
    @Mock
    private PersistenceService persistenceService;

    @InjectMocks
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {}

    @Test
    void saveMetrics() {
        // given
        String metricsName = "HYPERVOLUME";
        Experiment experiment = new Experiment();
        experiment.setId(1);
        int evaluationNumber = 100;
        float value = 123.45f;

        when(persistenceService.saveMetrics(any(Metrics.class)))
                .thenAnswer(invocation -> {
                    Metrics metrics = invocation.getArgument(0);
                    return metricsRepository.save(metrics);
                });

        // when
        metricsService.saveMetrics(metricsName, experiment, evaluationNumber, value);

        // then
        ArgumentCaptor<Metrics> captor = ArgumentCaptor.forClass(Metrics.class);
        verify(metricsRepository, times(1)).save(captor.capture());
        Metrics saved = captor.getValue();

        assertEquals("hypervolume", saved.getMetricsName(), "Metrika powinna być zapisana w lowercase");
        assertEquals(experiment, saved.getExperiment());
        assertEquals(evaluationNumber, saved.getIterationNumber());
        assertEquals(value, saved.getValue());
    }

    @Test
    void processMetricsNames_all() {
        // given
        Observations observations = mock(Observations.class);
        RunExperimentsRequest request = mock(RunExperimentsRequest.class);

        //  getMetrics() zwraca listę ["all"]
        when(request.metrics()).thenReturn(List.of("all"));

        Set<String> keys = new HashSet<>(Arrays.asList("Approximation Set", "Population", "hypervolume", "spacing"));
        when(observations.keys()).thenReturn(keys);

        // when
        Set<String> result = metricsService.processMetricsNames(observations, request);

        // then
        // zostaną ["hypervolume", "spacing"]
        assertTrue(result.contains("hypervolume"));
        assertTrue(result.contains("spacing"));
        assertFalse(result.contains("Approximation Set"));
        assertFalse(result.contains("Population"));
        assertEquals(2, result.size());
    }

    @Test
    void processMetricsNames_custom() {
        // given
        Observations observations = mock(Observations.class);
        RunExperimentsRequest request = mock(RunExperimentsRequest.class);
        when(request.metrics()).thenReturn(List.of("HV", "Spacing"));

        // np. gdy HV -> "hypervolume"
        try (MockedStatic<MetricsNameMapper> utilities = Mockito.mockStatic(MetricsNameMapper.class)) {
            utilities.when(() -> MetricsNameMapper.mapString("HV")).thenReturn("hypervolume");
            utilities.when(() -> MetricsNameMapper.mapString("Spacing")).thenReturn("spacing");

            // when
            Set<String> result = metricsService.processMetricsNames(observations, request);

            // then
            assertEquals(Set.of("hypervolume", "spacing"), result);
        }
    }

    @Test
    void saveAllMetrics() {
        // given
        Observations observations = mock(Observations.class);
        Observation obs1 = mock(Observation.class);
        Observation obs2 = mock(Observation.class);
        when(observations.iterator()).thenAnswer(inv -> Arrays.asList(obs1, obs2).iterator());

        when(obs1.get("hypervolume")).thenReturn(10.0);
        when(obs1.get("spacing")).thenReturn(20.0);
        when(obs1.getNFE()).thenReturn(100);

        when(obs2.get("hypervolume")).thenReturn(30.0);
        when(obs2.get("spacing")).thenReturn(40.0);
        when(obs2.getNFE()).thenReturn(200);

        Set<String> metricsNames = Set.of("hypervolume", "spacing");
        Experiment experiment = new Experiment();
        experiment.setId(9);

        when(persistenceService.saveMetrics(any(Metrics.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        metricsService.saveAllMetrics(observations, metricsNames, experiment);

        // then
        verify(persistenceService, times(4)).saveMetrics(any(Metrics.class));

        ArgumentCaptor<Metrics> captor = ArgumentCaptor.forClass(Metrics.class);
        verify(persistenceService, times(4)).saveMetrics(captor.capture());

        List<Metrics> allMetrics = captor.getAllValues();
        Set<String> combos = allMetrics.stream()
                .map(m -> m.getMetricsName() + "-" + m.getIterationNumber() + "-" + m.getValue())
                .collect(Collectors.toSet());

        assertTrue(combos.contains("hypervolume-100-10.0"));
        assertTrue(combos.contains("spacing-100-20.0"));
        assertTrue(combos.contains("hypervolume-200-30.0"));
        assertTrue(combos.contains("spacing-200-40.0"));
    }
}
