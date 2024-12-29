package com.example.Endpoint_Explorers.service;

import static org.junit.jupiter.api.Assertions.*;
import com.example.Endpoint_Explorers.mapper.MetricsNameMapper;
import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.Metrics;
import com.example.Endpoint_Explorers.repository.MetricsRepository;
import com.example.Endpoint_Explorers.request.RunExperimentRequest;
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

    @InjectMocks
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testSaveMetrics() {
        // given
        String metricsName = "HYPERVOLUME";
        Experiment experiment = new Experiment();
        experiment.setId(1);
        int evaluationNumber = 100;
        float value = 123.45f;

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
    void testProcessMetricsNames_all() {
        // given
        Observations observations = mock(Observations.class);
        RunExperimentRequest request = mock(RunExperimentRequest.class);

        //  getMetrics() zwraca listę ["all"]
        when(request.getMetrics()).thenReturn(List.of("all"));

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
    void testProcessMetricsNames_custom() {
        // given
        Observations observations = mock(Observations.class);
        RunExperimentRequest request = mock(RunExperimentRequest.class);
        when(request.getMetrics()).thenReturn(List.of("HV", "Spacing"));

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
    void testSaveAllMetrics() {
        // given
        Observations observations = mock(Observations.class);
        Observation obs1 = mock(Observation.class);
        Observation obs2 = mock(Observation.class);
        when(observations.iterator()).thenAnswer(inv -> Arrays.asList(obs1, obs2).iterator());

        // Metryki
        Set<String> metricsNames = Set.of("hypervolume", "spacing");
        Experiment experiment = new Experiment();
        experiment.setId(9);

        //  obs1.get("hypervolume") -> np. 10.0, obs1.get("spacing") -> 20.0, obs1.getNFE() -> 100
        when(obs1.get("hypervolume")).thenReturn(10.0);
        when(obs1.get("spacing")).thenReturn(20.0);
        when(obs1.getNFE()).thenReturn(100);

        // obs2.get("hypervolume") -> 30.0, obs2.get("spacing") -> 40.0, obs2.getNFE() -> 200
        when(obs2.get("hypervolume")).thenReturn(30.0);
        when(obs2.get("spacing")).thenReturn(40.0);
        when(obs2.getNFE()).thenReturn(200);

        // when
        metricsService.saveAllMetrics(observations, metricsNames, experiment);

        // then
        verify(metricsRepository, times(4)).save(any(Metrics.class));

        ArgumentCaptor<Metrics> captor = ArgumentCaptor.forClass(Metrics.class);
        verify(metricsRepository, times(4)).save(captor.capture());

        List<Metrics> allMetrics = captor.getAllValues();
        // Obs1, hypervolume => value=10, iteration=100
        // Obs1, spacing => value=20, iteration=100
        // Obs2, hypervolume => value=30, iteration=200
        // Obs2, spacing => value=40, iteration=200

        Set<String> combos = allMetrics.stream()
                .map(m -> m.getMetricsName()+"-"+m.getIterationNumber()+"-"+m.getValue())
                .collect(Collectors.toSet());
        assertTrue(combos.contains("hypervolume-100-10.0"));
        assertTrue(combos.contains("spacing-100-20.0"));
        assertTrue(combos.contains("hypervolume-200-30.0"));
        assertTrue(combos.contains("spacing-200-40.0"));
    }
}
