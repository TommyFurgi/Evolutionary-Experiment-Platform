package com.example.Endpoint_Explorers.component;

import com.example.Endpoint_Explorers.model.StatEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class StatisticsCalculator {
    public double calculateStat(List<Float> values, StatEnum statType) {
        double[] doubleValues = values.stream().mapToDouble(Float::doubleValue).toArray();

        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (double value : doubleValues) {
            stats.addValue(value);
        }

        return switch (statType) {
            case AVG -> calculateAverage(stats);
            case MEDIAN -> calculateMedian(stats);
            case STD_DEV -> calculateStandardDeviation(stats);
        };
    }

    private double calculateAverage(DescriptiveStatistics stats) {
        return stats.getMean();
    }

    private double calculateMedian(DescriptiveStatistics stats) {
        return stats.getPercentile(50);
    }

    private double calculateStandardDeviation(DescriptiveStatistics stats) {
        return stats.getStandardDeviation();
    }
}
