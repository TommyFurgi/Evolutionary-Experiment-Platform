package com.endpointexplorers.server.service;

import com.endpointexplorers.server.component.ExperimentObservableFactory;
import com.endpointexplorers.server.component.ExperimentValidator;
import com.endpointexplorers.server.model.Experiment;
import com.endpointexplorers.server.model.StatusEnum;
import com.endpointexplorers.server.repository.ExperimentRepository;
import com.endpointexplorers.server.request.ExperimentListRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    @Mock
    PersistenceService experimentSaveService;

    @InjectMocks
    ExperimentService experimentService;

    @BeforeEach
    void setUp() {}

    @Test
    void getExperimentByIdReadyStatus() {
        // given
        Experiment readyExp = new Experiment();
        readyExp.setId(123);
        readyExp.setStatus(StatusEnum.READY);

        when(repository.findById(123)).thenReturn(Optional.of(readyExp));
        when(experimentSaveService.saveExperiment(any(Experiment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Optional<Experiment> opt = experimentService.getExperimentById(123);

        // then
        assertTrue(opt.isPresent());
        assertEquals(StatusEnum.COMPLETED, opt.get().getStatus());
        verify(experimentSaveService, times(1)).saveExperiment(readyExp);
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
        when(experimentSaveService.saveExperiment(any(Experiment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<Experiment> result = experimentService.getReadyExperiments();

        // then
        assertEquals(2, result.size());

        ArgumentCaptor<Experiment> captor = ArgumentCaptor.forClass(Experiment.class);
        verify(experimentSaveService, times(2)).saveExperiment(any(Experiment.class));
        List<Experiment> savedExps = captor.getAllValues();
        savedExps.forEach(exp -> assertEquals(StatusEnum.COMPLETED, exp.getStatus()));
    }

    @Test
    void getFilteredExperiments() {
        // given
        List<String> statuses = List.of("READY", "IN_PROGRESS");
        List<String> problems = List.of("UF1");
        List<String> algorithms = List.of("nsga-ii");
        List<String> metrics = List.of("spacing");
        List<String> groupNames = List.of("none");
        ExperimentListRequest experimentListRequest = new ExperimentListRequest(problems, algorithms, metrics, statuses, groupNames);

        doNothing().when(validator).validateListParams(statuses, problems, algorithms, metrics);
        when(repository.findFilteredExperiments(
                anySet(), anySet(), anySet(), anySet(), anySet()
        )).thenReturn(List.of());

        // when
        when(metricsService.parseMetricsName("spacing")).thenReturn("spacing");  // Mocking to return "spacing"
        List<Experiment> result = experimentService.getFilteredExperiments(experimentListRequest);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(validator, times(1)).validateListParams(statuses, problems, algorithms, metrics);
        verify(repository, times(1)).findFilteredExperiments(
                argThat(s -> s.contains("READY") && s.contains("IN_PROGRESS")),
                argThat(p -> p.contains("uf1")),
                argThat(a -> a.contains("nsga-ii")),
                argThat(m -> m.contains("spacing")),
                argThat(g -> g.contains("none"))
        );
    }

    @Test
    void updateGroupForEmptyExperimentsList() {
        // given
        List<Integer> experimentIds = List.of();
        String newGroupName = "groupA";

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            experimentService.updateGroupForExperiments(experimentIds, newGroupName);
        });
        assertEquals("Experiment IDs cannot be null or empty", exception.getMessage());
    }

    @Test
    void updateGroupForExperimentsList() {
        // given
        List<Integer> experimentIds = List.of(1, 2, 3);
        String newGroupName = "groupA";

        Experiment experiment1 = new Experiment();
        experiment1.setId(1);
        Experiment experiment2 = new Experiment();
        experiment2.setId(2);
        Experiment experiment3 = new Experiment();
        experiment3.setId(3);

        when(repository.findAllById(experimentIds)).thenReturn(List.of(experiment1, experiment2, experiment3));
        doNothing().when(experimentSaveService).updateExperimentsGroup(anyList(), eq(newGroupName));

        // when
        List<Integer> updatedExperimentIds = experimentService.updateGroupForExperiments(experimentIds, newGroupName);

        // then
        assertEquals(3, updatedExperimentIds.size());
        assertTrue(updatedExperimentIds.containsAll(experimentIds));

        verify(experimentSaveService, times(1)).updateExperimentsGroup(experimentIds, newGroupName);
    }

}
