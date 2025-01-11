package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.component.ExperimentObservableFactory;
import com.example.Endpoint_Explorers.component.ExperimentValidator;
import com.example.Endpoint_Explorers.component.PlotFactory;
import com.example.Endpoint_Explorers.component.StatisticsCalculator;
import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.Metrics;
import com.example.Endpoint_Explorers.model.StatEnum;
import com.example.Endpoint_Explorers.model.StatusEnum;
import com.example.Endpoint_Explorers.repository.ExperimentRepository;
import com.example.Endpoint_Explorers.repository.MetricsRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private record TimeRange(Timestamp start, Timestamp end) {
    }

    private final ExperimentRepository experimentRepository;
    private final MetricsRepository metricsRepository;
    private final StatisticsCalculator statisticsCalculator;
    private final ExperimentValidator validator;
    private Map<String, List<Double>> resultMetricsMap;
    @Getter
    private List<String> fileNamePaths = new ArrayList<>();
    private Timestamp startDate;
    private Timestamp endDate;

    public Map<String, List<Double>> getStatsTimeFromInterval(String problemName, String algorithm, String start, String end, String statType, List<String> metricsNames) {
        StatEnum enumStatType = StatEnum.extractStatsType(statType);

        TimeRange timestamps = parseTimestamps(start, end);
        startDate = timestamps.start;
        endDate = timestamps.end;

        validator.validateStatsParams(problemName, algorithm, startDate, endDate);

        List<Experiment> experiments = extractExperiments(algorithm, problemName, startDate, endDate);

        int maxPossibleEvaluation = getMaxPossibleEvaluation(experiments);

        List<Metrics> metricsList = getMetricsList(experiments, maxPossibleEvaluation);

        int maxIteration = maxPossibleEvaluation / ExperimentObservableFactory.getFrequency();

        Map<String, List<List<Float>>> metricsMap = createMetricsMap(metricsList, maxIteration);

        createResultMetricsMap(metricsMap, maxIteration, enumStatType);

        generatePlot(metricsNames, algorithm, problemName, createIterations(maxIteration));
        return resultMetricsMap;
    }

    private TimeRange parseTimestamps(String start, String end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

        LocalDateTime startLocal = LocalDateTime.parse(start, formatter);
        LocalDateTime endLocal = LocalDateTime.parse(end, formatter);

        Timestamp startDate = Timestamp.valueOf(startLocal);
        Timestamp endDate = Timestamp.valueOf(endLocal);

        System.out.println("Start Timestamp: " + startDate + ", End Timestamp: " + endDate);
        return new TimeRange(startDate, endDate);
    }

    private List<Experiment> extractExperiments(String algorithm, String problemName, Timestamp startDate, Timestamp endDate) {
        List<Experiment> experiments = experimentRepository.findByAlgorithmAndProblemNameAndStatusAndDatetimeBetween(
                algorithm.toLowerCase(), problemName.toLowerCase(), StatusEnum.COMPLETED, startDate, endDate);

        if (experiments.isEmpty()) {
            throw new IllegalArgumentException("No experiments found in the specified time interval.");
        }
        System.out.println("ID of experiments included in our stats table: " + experiments.stream()
                .map(Experiment::getId)
                .toList());
        return experiments;
    }

    private int getMaxPossibleEvaluation(List<Experiment> experiments) {
        return experiments.stream()
                .mapToInt(Experiment::getNumberOfEvaluation)
                .min()
                .orElse(0);
    }

    private List<Metrics> getMetricsList(List<Experiment> experiments, int maxPossibleEvaluation) {
        List<Metrics> metricsList = new ArrayList<>();
        for (Experiment experiment : experiments) {
            metricsRepository.findByExperimentId(experiment.getId()).stream()
                    .filter(metrics -> metrics.getIterationNumber() <= maxPossibleEvaluation)
                    .forEach(metricsList::add);
        }
        return metricsList;
    }

    private Map<String, List<List<Float>>> createMetricsMap(List<Metrics> metricsList, int maxIteration) {
        Map<String, List<List<Float>>> metricsMap = new HashMap<>();

        for (Metrics metrics : metricsList) {
            String metricsName = metrics.getMetricsName();
            int iterationNumber = (metrics.getIterationNumber() / 100) - 1;
            float value = metrics.getValue();
            metricsMap.computeIfAbsent(metricsName, values -> {
                List<List<Float>> outerList = new ArrayList<>(maxIteration);
                for (int i = 0; i < maxIteration; i++) {
                    outerList.add(new ArrayList<>());
                }
                return outerList;
            });

            List<List<Float>> outerList = metricsMap.get(metricsName);
            outerList.get(iterationNumber).add(value);
        }
        return metricsMap;
    }

    private void createResultMetricsMap(Map<String, List<List<Float>>> metricsMap, int maxIteration, StatEnum enumStatType) {
        resultMetricsMap = new HashMap<>();

        for (String metricsName : metricsMap.keySet()) {
            for (int i = 0; i < maxIteration; i++) {
                resultMetricsMap.computeIfAbsent(metricsName, k -> new ArrayList<>())
                        .add(statisticsCalculator.calculateStat(metricsMap.get(metricsName).get(i), enumStatType));
            }
        }
    }

    private List<Integer> createIterations(int maxIteration) {
        List<Integer> iterations = new ArrayList<>();
        int frequency = ExperimentObservableFactory.getFrequency();
        for (int i = 1; i <= maxIteration; i++) {
            iterations.add(i * frequency);
        }
        return iterations;
    }

    public void generatePlot(List<String> metricsNames, String algorithmName, String problemName, List<Integer> iterations) {
        fileNamePaths = new ArrayList<>();
        if (metricsNames.size() == 1 && metricsNames.getFirst().equals("all")) {
            for (String key : resultMetricsMap.keySet()) {
                String plotPath = PlotFactory.createPlot(key, algorithmName, problemName, startDate, endDate, iterations, resultMetricsMap.get(key));
                fileNamePaths.add(plotPath);
            }
        } else if (metricsNames.size() != 1) {
            for (String key : metricsNames) {
                String plotPath = PlotFactory.createPlot(key, algorithmName, problemName, startDate, endDate, iterations, resultMetricsMap.get(key));
                fileNamePaths.add(plotPath);
            }
        } else if (!metricsNames.getFirst().equals("none")) {
            String plotPath = PlotFactory.createPlot(metricsNames.getFirst(), algorithmName, problemName, startDate, endDate, iterations, resultMetricsMap.get(metricsNames.getFirst()));
            fileNamePaths.add(plotPath);
        }
    }
}