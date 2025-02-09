package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.component.ExperimentValidator;
import com.example.Endpoint_Explorers.component.StatisticsCalculator;
import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.Metrics;
import com.example.Endpoint_Explorers.model.StatEnum;
import com.example.Endpoint_Explorers.model.StatusEnum;
import com.example.Endpoint_Explorers.repository.ExperimentRepository;
import com.example.Endpoint_Explorers.repository.MetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private MetricsRepository metricsRepository;

    @Mock
    private StatisticsCalculator statisticsCalculator;

    @Mock
    private ExperimentValidator validator;

    @InjectMocks
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getStatsTimeFromInterval_success() {
        // given
        String problemName = "UF1";
        String algorithm = "NSGA-II";
        String startStr = "2024-01-01_00:00:00";
        String endStr = "2024-12-31_23:59:59";
        String statType = "MEDIAN";

        Experiment exp1 = new Experiment();
        exp1.setId(1);
        exp1.setAlgorithm(algorithm.toLowerCase());
        exp1.setProblemName(problemName.toLowerCase());
        exp1.setNumberOfEvaluation(200);
        exp1.setStatus(StatusEnum.COMPLETED);

        Experiment exp2 = new Experiment();
        exp2.setId(2);
        exp2.setAlgorithm(algorithm.toLowerCase());
        exp2.setProblemName(problemName.toLowerCase());
        exp2.setNumberOfEvaluation(200);
        exp2.setStatus(StatusEnum.COMPLETED);

        List<Experiment> experiments = List.of(exp1, exp2);

        when(experimentRepository.findByAlgorithmAndProblemNameAndStatusAndDatetimeBetween(
                eq("nsga-ii"), eq("uf1"), eq(StatusEnum.COMPLETED), any(Timestamp.class), any(Timestamp.class), eq(""))
        ).thenReturn(experiments);

        //  iteration=100 => index=0, iteration=200 => index=1
        Metrics m1 = new Metrics();
        m1.setExperiment(exp1);
        m1.setIterationNumber(100);
        m1.setMetricsName("hypervolume");
        m1.setValue(10.0f);

        Metrics m2 = new Metrics();
        m2.setExperiment(exp2);
        m2.setIterationNumber(100);
        m2.setMetricsName("hypervolume");
        m2.setValue(20.0f);

        Metrics m3 = new Metrics();
        m3.setExperiment(exp1);
        m3.setIterationNumber(200);
        m3.setMetricsName("hypervolume");
        m3.setValue(15.0f);

        Metrics m4 = new Metrics();
        m4.setExperiment(exp2);
        m4.setIterationNumber(200);
        m4.setMetricsName("hypervolume");
        m4.setValue(25.0f);

        when(metricsRepository.findByExperimentId(1)).thenReturn(List.of(m1, m3));
        when(metricsRepository.findByExperimentId(2)).thenReturn(List.of(m2, m4));

        doNothing().when(validator).validateStatsParams(anyString(), anyString(), any(), any(), anyList());

        // symulacja liczenia MEDIAN:
        // iteration=100 => [10,20] => median=15
        when(statisticsCalculator.calculateStat(eq(List.of(10f, 20f)), eq(StatEnum.MEDIAN)))
                .thenReturn(15.0);

        // iteration=200 => [15,25] => median=20
        when(statisticsCalculator.calculateStat(eq(List.of(15f, 25f)), eq(StatEnum.MEDIAN)))
                .thenReturn(20.0);

        // when
        Map<String, List<Double>> resultMap = statisticsService.getStatsTimeFromInterval(
                problemName, algorithm, startStr, endStr, statType, new ArrayList<>(), "");

        // then
        assertTrue(resultMap.containsKey("hypervolume"));
        List<Double> values = resultMap.get("hypervolume");

        assertEquals(2, values.size());
        assertEquals(15.0, values.get(0));
        assertEquals(20.0, values.get(1));

        verify(validator).validateStatsParams(eq(problemName), eq(algorithm), any(), any(), anyList());
    }

    @Test
    void getStatsTimeFromInterval_noExperiments() {
        // given
        String problemName = "UF1";
        String algorithm = "NSGA-II";
        String startStr = "2024-01-01_00:00:00";
        String endStr = "2024-12-31_23:59:59";
        String statType = "AVG";

        when(experimentRepository.findByAlgorithmAndProblemNameAndStatusAndDatetimeBetween(
                anyString(), anyString(), eq(StatusEnum.COMPLETED), any(), any(), anyString()
        )).thenReturn(Collections.emptyList());

        doNothing().when(validator).validateStatsParams(anyString(), anyString(), any(), any(), any());

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                statisticsService.getStatsTimeFromInterval(problemName, algorithm, startStr, endStr, statType, null, "")
        );
    }

    @Test
    void getStatsTimeFromInterval_statTypeStdDev() {
        String problemName = "DTLZ2";
        String algorithm = "GDE3";
        String startStr = "2025-01-01_00:00:00";
        String endStr = "2025-12-31_23:59:59";
        String statType = "STD_DEV";

        Experiment exp = new Experiment();
        exp.setId(10);
        exp.setAlgorithm("gde3");
        exp.setProblemName("dtlz2");
        exp.setNumberOfEvaluation(100); // iteration => maxIteration=1
        exp.setStatus(StatusEnum.COMPLETED);

        when(experimentRepository.findByAlgorithmAndProblemNameAndStatusAndDatetimeBetween(
                eq("gde3"), eq("dtlz2"), eq(StatusEnum.COMPLETED), any(), any(), eq("")
        )).thenReturn(List.of(exp));

        Metrics m1 = new Metrics();
        m1.setExperiment(exp);
        m1.setIterationNumber(100);  // index= (100/100)-1=0
        m1.setMetricsName("hypervolume");
        m1.setValue(11.0f);

        when(metricsRepository.findByExperimentId(10))
                .thenReturn(List.of(m1));

        doNothing().when(validator).validateStatsParams(anyString(), anyString(), any(), any(), anyList());

        when(statisticsCalculator.calculateStat(eq(List.of(11f)), eq(StatEnum.STD_DEV)))
                .thenReturn(11.0);

        // when
        Map<String, List<Double>> result = statisticsService.getStatsTimeFromInterval(
                problemName, algorithm, startStr, endStr, statType, new ArrayList<>(), "");

        // then
        assertTrue(result.containsKey("hypervolume"));
        List<Double> hvValues = result.get("hypervolume");
        // maxIteration= 100/100=1 => 1 element
        assertEquals(1, hvValues.size());
        assertEquals(11.0, hvValues.get(0));
    }

    @Test
    void getStatsTimeFromInterval_validatorThrows() {
        doThrow(new IllegalArgumentException("Wrong date interval"))
                .when(validator).validateStatsParams(anyString(), anyString(), any(), any(), anyList());

        assertThrows(IllegalArgumentException.class, () ->
                statisticsService.getStatsTimeFromInterval("UF1", "e-MOEA",
                        "2024-01-01_00:00:00", "2024-12-31_23:59:59", "AVG", new ArrayList<>(), "")
        );
    }

    @Test
    void getStatsTimeFromInterval_parseTimestamps() {
        String startStr = "2024-01-01_12:34:56";
        String endStr = "2024-01-02_01:02:03";

        // musimy zwrócić cokolwiek, żeby nie rzuciło noExperiments
        Experiment dummyExp = new Experiment();
        dummyExp.setId(99);
        dummyExp.setStatus(StatusEnum.COMPLETED);
        dummyExp.setNumberOfEvaluation(100);
        when(experimentRepository.findByAlgorithmAndProblemNameAndStatusAndDatetimeBetween(
                anyString(), anyString(), eq(StatusEnum.COMPLETED), any(), any(), anyString()
        )).thenReturn(List.of(dummyExp));

        doNothing().when(validator).validateStatsParams(anyString(), anyString(), any(), any(), anyList());

        // Zwracamy pustą listę metryk => finalny result jest pustą mapą
        when(metricsRepository.findByExperimentId(99)).thenReturn(Collections.emptyList());

        // when
        Map<String, List<Double>> result = statisticsService.getStatsTimeFromInterval(
                "ZDT3", "NSGA-II", startStr, endStr, "AVG", new ArrayList<>(), ""
        );

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty(), "bez metryk, result powinien być pusty");
    }
}
