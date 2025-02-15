package com.endpointexplorers.server.service;

import com.endpointexplorers.server.component.*;
import com.endpointexplorers.server.model.*;
import com.endpointexplorers.server.repository.ExperimentRepository;
import com.endpointexplorers.server.repository.MetricsRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class StatisticsService {
    private record TimeRange(Timestamp start, Timestamp end) {
    }

    private final ExperimentRepository experimentRepository;
    private final MetricsRepository metricsRepository;
    private final StatisticsCalculator statisticsCalculator;
    private final ExperimentValidator validator;
    private final PlotService plotService;
    private final CsvService csvService;
    private final MetricsService metricsService;
    private final FileContentConverter fileContentConverter;
    private Map<String, List<Double>> resultMetricsMap;
    @Getter
    private Timestamp startDate;
    @Getter
    private Timestamp endDate;

    public MetricsAndFiles getStats(String problemName,
                                    String algorithm,
                                    String start,
                                    String end,
                                    String statType,
                                    List<String> metricsNames,
                                    String groupName,
                                    boolean isPlot,
                                    boolean isCsv) {

        Map<String, List<Double>> metricsResults = getStatsTimeFromInterval(
                problemName, algorithm, start, end, statType, metricsNames, groupName
        );

        List<FileDetails> files = new ArrayList<>();
        metricsNames = metricsService.parseMetricsList(metricsNames);

        if (isPlot) {
            int maxIteration = getMaxIteration(metricsResults);
            List<Integer> iterations = createIterations(maxIteration);

            List<String> plotPaths = plotService.generatePlots(
                    metricsResults,
                    metricsNames,
                    algorithm,
                    problemName,
                    iterations
            );
            files.addAll(fileContentConverter.createFilesDetails(plotPaths));
        }

        if (isCsv) {
            FileDetails csvFile = csvService.createCsv(
                    metricsResults,
                    problemName,
                    algorithm
            );
            files.add(csvFile);
        }

        return new MetricsAndFiles(metricsResults, files);
    }

    public Map<String, List<Double>> getStatsTimeFromInterval(
            String problemName,
            String algorithm,
            String start,
            String end,
            String statType,
            List<String> metricsNames,
            String groupName
    ) {
        TimeRange timestamps = parseTimestamps(start, end);
        startDate = timestamps.start;
        endDate = timestamps.end;

        validator.validateStatsParams(problemName, algorithm, startDate, endDate, metricsNames);

        List<Experiment> experiments = extractExperiments(algorithm, problemName, startDate, endDate, groupName);
        int maxPossibleEvaluation = getMaxPossibleEvaluation(experiments);
        List<Metrics> metricsList = getMetricsList(experiments, maxPossibleEvaluation);

        int maxIteration = maxPossibleEvaluation / ExperimentObservableFactory.getFrequency();
        Map<String, List<List<Float>>> metricsMap = createMetricsMap(metricsList, maxIteration);

        StatEnum enumStatType = StatEnum.extractStatsType(statType);
        createResultMetricsMap(metricsMap, maxIteration, enumStatType);

        return resultMetricsMap;
    }

    private TimeRange parseTimestamps(String start, String end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

        LocalDateTime startLocal = LocalDateTime.parse(start, formatter);
        LocalDateTime endLocal = LocalDateTime.parse(end, formatter);

        Timestamp startDate = Timestamp.valueOf(startLocal);
        Timestamp endDate = Timestamp.valueOf(endLocal);

        return new TimeRange(startDate, endDate);
    }

    private List<Experiment> extractExperiments(String algorithm, String problemName, Timestamp startDate, Timestamp endDate, String groupName) {
        List<Experiment> experiments = experimentRepository.findByAlgorithmAndProblemNameAndStatusAndDatetimeBetween(
                algorithm.toLowerCase(), problemName.toLowerCase(), StatusEnum.COMPLETED, startDate, endDate, groupName);

        if (experiments.isEmpty()) {
            throw new IllegalArgumentException("No experiments found in the specified time interval.");
        }
        log.info("ID of experiments included in our stats table: " + experiments.stream()
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

    public List<Integer> createIterations(int maxIteration) {

        List<Integer> iterations = new ArrayList<>();
        int frequency = ExperimentObservableFactory.getFrequency();
        for (int i = 1; i <= maxIteration; i++) {
            iterations.add(i * frequency);
        }
        return iterations;
    }
    private int getMaxIteration(Map<String, List<Double>> metricsResults) {
        if (!metricsResults.isEmpty()) {
            String firstMetric = metricsResults.keySet().iterator().next();
            return metricsResults.get(firstMetric).size();
        }
        return 0;
    }
}