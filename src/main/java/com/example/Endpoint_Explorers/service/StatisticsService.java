package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.Metrics;
import com.example.Endpoint_Explorers.model.StatusEnum;
import com.example.Endpoint_Explorers.repository.ExperimentRepository;
import com.example.Endpoint_Explorers.repository.MetricsRepository;
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
    private final ExperimentRepository experimentRepository;
    private final MetricsRepository metricsRepository;

    public Map<String, List<Double>> getStatsTimeFromInterval(String problemName, String algorithm, String start, String end) {
//      Correct DateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime startLocal = LocalDateTime.parse(start, formatter);
        LocalDateTime endLocal = LocalDateTime.parse(end, formatter);

        Timestamp startDate = Timestamp.valueOf(startLocal);
        Timestamp endDate = Timestamp.valueOf(endLocal);

        System.out.println("Start Timestamp: " + startDate + ", End Timestamp: " + endDate);

//      Experiment List
//      XDDDDD
        List<Experiment> experiments = experimentRepository.findByAlgorithmAndProblemNameAndStatusAndDatetimeBetween(algorithm, problemName, StatusEnum.COMPLETED, startDate, endDate);

        if (experiments.isEmpty()) {
            throw new IllegalArgumentException("No experiments found in the specified time interval.");
        }
        System.out.println("ID of experiments included in our stats table: " + experiments.stream().map(Experiment::getId).toList());

//      Max number of evaluation we should consider
        int maxPossibleEvaluation = experiments.stream().mapToInt(Experiment::getNumberOfEvaluation).min().orElse(0);

        Map<String, List<List<Float>>> metricsMap = new HashMap<>();
        Map<String, List<Double>> resultMetricsMap = new HashMap<>();

        List<Metrics> metricsList = new ArrayList<>();
        for (Experiment experiment : experiments) {
            metricsRepository.findByExperimentId(experiment.getId()).stream()
                    .filter(metrics -> metrics.getIterationNumber() <= maxPossibleEvaluation)
                    .forEach(metricsList::add);
        }

        int maxIteration = maxPossibleEvaluation / 100;
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

//      Creating HashMap of results
        for (String metricsName : metricsMap.keySet()) {
            for (int i = 0; i < maxIteration; i++) {
                resultMetricsMap.computeIfAbsent(metricsName, k -> new ArrayList<>()).add(calculateStat(metricsMap.get(metricsName).get(i)));
            }
        }
        return resultMetricsMap;
    }

    private double calculateStat(List<Float> values) {
        return calculateAverage(values);
    }

    private double calculateAverage(List<Float> values) {
        return values.stream().mapToDouble(Float::floatValue).average().orElse(0.0);
    }

    private double calculateMedian(List<Double> metrics) {
        List<Double> sorted = metrics.stream().sorted().toList();
        int size = sorted.size();
        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }

    private double calculateStandardDeviation(List<Float> metrics, double mean) {
        double variance = metrics.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }
}

