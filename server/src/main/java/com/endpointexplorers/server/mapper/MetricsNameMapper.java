package com.endpointexplorers.server.mapper;

import java.util.HashMap;
import java.util.Map;

public class MetricsNameMapper {

    private static final Map<String, String> STRING_MAPPING = new HashMap<>();

    static {
        STRING_MAPPING.put("elapsed-time", "Elapsed Time");
        STRING_MAPPING.put("nfe", "NFE");
        STRING_MAPPING.put("additive-epsilon-indicator", "AdditiveEpsilonIndicator");
        STRING_MAPPING.put("archive-size", "Archive Size");
        STRING_MAPPING.put("contribution", "Contribution");
        STRING_MAPPING.put("generational-distance", "GenerationalDistance");
        STRING_MAPPING.put("generational-distance-plus", "GenerationalDistancePlus");
        STRING_MAPPING.put("hypervolume", "Hypervolume");
        STRING_MAPPING.put("inverted-generational-distance", "InvertedGenerationalDistance");
        STRING_MAPPING.put("inverted-generational-distance-plus", "InvertedGenerationalDistancePlus");
        STRING_MAPPING.put("maximum-pareto-front-error", "MaximumParetoFrontError");
        STRING_MAPPING.put("number-of-dominating-improvements", "Number of Dominating Improvements");
        STRING_MAPPING.put("number-of-improvements", "Number of Improvements");
        STRING_MAPPING.put("population-size", "Population Size");
        STRING_MAPPING.put("r1-indicator", "R1Indicator");
        STRING_MAPPING.put("r2-indicator", "R2Indicator");
        STRING_MAPPING.put("r3-indicator", "R3Indicator");
        STRING_MAPPING.put("spacing", "Spacing");
    }

    public static String mapString(String input) {
        return STRING_MAPPING.getOrDefault(input, input);
    }
}