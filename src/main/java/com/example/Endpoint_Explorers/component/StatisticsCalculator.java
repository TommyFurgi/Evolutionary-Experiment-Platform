package com.example.Endpoint_Explorers.component;

import com.example.Endpoint_Explorers.model.StatEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class StatisticsCalculator {
    public double calculateStat(List<Float> values, StatEnum statType) {
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
