package com.example.Endpoint_Explorers.model;

import org.moeaframework.Instrumenter;

import java.util.Optional;
import java.util.function.Consumer;

public enum MetricTypeEnum {
    ALL(Instrumenter::attachAll),
    ELAPSED_TIME(Instrumenter::attachElapsedTimeCollector),
    HYPERVOLUME(Instrumenter::attachHypervolumeCollector),
    GENERATIONAL_DISTANCE(Instrumenter::attachGenerationalDistanceCollector),
    GENERATIONAL_DISTANCE_PLUS(Instrumenter::attachGenerationalDistancePlusCollector),
    INVERTED_GENERATIONAL_DISTANCE(Instrumenter::attachInvertedGenerationalDistanceCollector),
    INVERTED_GENERATIONAL_DISTANCE_PLUS(Instrumenter::attachInvertedGenerationalDistancePlusCollector),
    SPACING(Instrumenter::attachSpacingCollector),
    ADDITIVE_EPSILON_INDICATOR(Instrumenter::attachAdditiveEpsilonIndicatorCollector),
    CONTRIBUTION(Instrumenter::attachContributionCollector),
    MAXIMUM_PARETO_FRONT_ERROR(Instrumenter::attachMaximumParetoFrontErrorCollector),
    R1_INDICATOR(Instrumenter::attachR1Collector),
    R2_INDICATOR(Instrumenter::attachR2Collector),
    R3_INDICATOR(Instrumenter::attachR3Collector),
    NUMBER_OF_DOMINATING_IMPROVEMENTS(Instrumenter::attachEpsilonProgressCollector),
    NUMBER_OF_IMPROVEMENTS(Instrumenter::attachAdaptiveMultimethodVariationCollector),
    ADAPTIVE_TIME_CONTINUATION(Instrumenter::attachAdaptiveTimeContinuationCollector),
    APPROXIMATION_SET(Instrumenter::attachApproximationSetCollector),
    POPULATION(Instrumenter::attachPopulationCollector),
    POPULATION_SIZE(Instrumenter::attachPopulationSizeCollector);

    private final Consumer<Instrumenter> attachMethod;

    MetricTypeEnum(Consumer<Instrumenter> attachMethod) {
        this.attachMethod = attachMethod;
    }

    public void attach(Instrumenter instrumenter) {
        attachMethod.accept(instrumenter);
    }

    public static Optional<MetricTypeEnum> fromString(String name) {
        try {
            return Optional.of(MetricTypeEnum.valueOf(name.toUpperCase().replace("-", "_")));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
