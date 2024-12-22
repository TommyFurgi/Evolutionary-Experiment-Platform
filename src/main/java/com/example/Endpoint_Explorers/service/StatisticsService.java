package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.Metrics;
import com.example.Endpoint_Explorers.model.StatEnum;
import com.example.Endpoint_Explorers.model.StatusEnum;
import com.example.Endpoint_Explorers.repository.ExperimentRepository;
import com.example.Endpoint_Explorers.repository.MetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final ExperimentRepository experimentRepository;
    private final MetricsRepository metricsRepository;

    public Map<String, List<Double>> getStatsTimeFromInterval(String problemName, String algorithm, String start, String end, String statType) {
        StatEnum enumStatType = extractStatsType(statType);

        Timestamp[] timestamps = parseTimestamps(start, end);
        Timestamp startDate = timestamps[0];
        Timestamp endDate = timestamps[1];

        List<Experiment> experiments = extractExperiments(algorithm, problemName, startDate, endDate);

        int maxPossibleEvaluation = getMaxPossibleEvaluation(experiments);

        List<Metrics> metricsList = getMetricsList(experiments, maxPossibleEvaluation);

        int maxIteration = maxPossibleEvaluation / 100;

        Map<String, List<List<Float>>> metricsMap = createMetricsMap(metricsList, maxIteration);
        return createResultMetricsMap(metricsMap, maxIteration, enumStatType);
    }

    private StatEnum extractStatsType(String statType) {
        try {
            return StatEnum.valueOf(statType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Incorrect statistics type: " + statType
                    + " Choose one of the following: " + Arrays.toString(StatEnum.values()));
        }
    }

    private Timestamp[] parseTimestamps(String start, String end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime startLocal = LocalDateTime.parse(start, formatter);
        LocalDateTime endLocal = LocalDateTime.parse(end, formatter);

        Timestamp startDate = Timestamp.valueOf(startLocal);
        Timestamp endDate = Timestamp.valueOf(endLocal);

        System.out.println("Start Timestamp: " + startDate + ", End Timestamp: " + endDate);
        return new Timestamp[]{startDate, endDate};
    }

    private List<Experiment> extractExperiments(String algorithm, String problemName, Timestamp startDate, Timestamp endDate) {
        List<Experiment> experiments = experimentRepository.findByAlgorithmAndProblemNameAndStatusAndDatetimeBetween(
                algorithm, problemName, StatusEnum.COMPLETED, startDate, endDate);

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
            metricsMap.computeIfAbsent(metricsName, k -> {
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

    private Map<String, List<Double>> createResultMetricsMap(Map<String, List<List<Float>>> metricsMap, int maxIteration, StatEnum enumStatType) {
        Map<String, List<Double>> resultMetricsMap = new HashMap<>();

        for (String metricsName : metricsMap.keySet()) {
            for (int i = 0; i < maxIteration; i++) {
                resultMetricsMap.computeIfAbsent(metricsName, k -> new ArrayList<>())
                        .add(calculateStat(metricsMap.get(metricsName).get(i), enumStatType));
            }
        }
        return resultMetricsMap;
    }

    private double calculateStat(List<Float> values, StatEnum statType) {
        return switch (statType) {
            case AVG -> calculateAverage(values);
            case MEDIAN -> calculateMedian(values);
            case STD_DEV -> {
                double mean = calculateAverage(values);
                yield calculateStandardDeviation(values, mean);
            }
        };
    }

    private double calculateAverage(List<Float> values) {
        return values.stream().mapToDouble(Float::floatValue).average().orElse(0.0);
    }

    private double calculateMedian(List<Float> values) {
        List<Float> sorted = values.stream().sorted().toList();
        int size = sorted.size();
        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }

    private double calculateStandardDeviation(List<Float> values, double mean) {
        double variance = values.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }
}

