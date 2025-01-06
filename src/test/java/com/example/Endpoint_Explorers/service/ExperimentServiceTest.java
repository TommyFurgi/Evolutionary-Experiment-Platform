package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.component.ExperimentObservableFactory;
import com.example.Endpoint_Explorers.component.ExperimentValidator;
import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.StatusEnum;
import com.example.Endpoint_Explorers.repository.ExperimentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExperimentServiceTest {

    @Mock
    ExperimentObservableFactory observableFactory;
    @Mock
    ExperimentRepository repository;
    @Mock
    MetricsService metricsService;
    @Mock
    ExperimentValidator validator;
    @InjectMocks
    ExperimentService experimentService;

    @Test
    void getExperimentByIdReadyStatus() {
        // given
        Experiment readyExp = new Experiment();
        readyExp.setId(123);
        readyExp.setStatus(StatusEnum.READY);

        when(repository.findById(123)).thenReturn(Optional.of(readyExp));
        when(repository.save(readyExp)).thenReturn(readyExp);

        // when
        Optional<Experiment> opt = experimentService.getExperimentById(123);

        // then
        assertTrue(opt.isPresent());
        assertEquals(StatusEnum.COMPLETED, opt.get().getStatus());
        verify(repository, times(1)).save(readyExp);
    }

    @Test
    void getReadyExperiments() {
        // given
        Experiment e1 = new Experiment();
        e1.setId(1);
        e1.setStatus(StatusEnum.READY);
        Experiment e2 = new Experiment();
        e2.setId(2);
        e2.setStatus(StatusEnum.READY);

        when(repository.findByStatus(StatusEnum.READY)).thenReturn(List.of(e1, e2));

        // when
        List<Experiment> result = experimentService.getReadyExperiments();

        // then
        assertEquals(2, result.size());

        ArgumentCaptor<Experiment> captor = ArgumentCaptor.forClass(Experiment.class);
        verify(repository, times(2)).save(captor.capture());
        List<Experiment> savedExps = captor.getAllValues();
        savedExps.forEach(exp -> assertEquals(StatusEnum.COMPLETED, exp.getStatus()));
    }

    @Test
    void getFilteredExperiments() {
        // given
        List<String> statuses = List.of("READY", "IN_PROGRESS");
        List<String> problems = List.of("UF1");
        List<String> algos = List.of("nsga-ii");
        List<String> metrics = List.of("spacing");

        doNothing().when(validator).validateListParams(statuses, problems, algos, metrics);
        when(repository.findFilteredExperiments(
                anySet(), anySet(), anySet(), anySet()
        )).thenReturn(List.of());

        // when
        when(metricsService.parseMetricsName("spacing")).thenReturn("spacing");  // Mocking to return "spacing"
        List<Experiment> result = experimentService.getFilteredExperiments(statuses, problems, algos, metrics);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(validator, times(1)).validateListParams(statuses, problems, algos, metrics);
        verify(repository, times(1)).findFilteredExperiments(
                argThat(s -> s.contains("READY") && s.contains("IN_PROGRESS")),
                argThat(p -> p.contains("uf1")),
                argThat(a -> a.contains("nsga-ii")),
                argThat(m -> m.contains("spacing"))
        );
    }
}
