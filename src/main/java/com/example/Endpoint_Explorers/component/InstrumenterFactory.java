package com.example.Endpoint_Explorers.component;

import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.Instrumenter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InstrumenterFactory {

    public Instrumenter createInstrumenter(RunExperimentRequest request) {
        Instrumenter instrumenter = new Instrumenter()
                .withProblem(request.getProblemName())
                .withFrequency(100);

        for (String metric : request.getMetrics()) {
            System.out.println("metric: " + metric);
            switch (metric) {
                case "all":
                    instrumenter.attachAll();
                    break;
                case "elapsed-time":
                    instrumenter.attachElapsedTimeCollector();
                    break;
                case "hyper-volume":
                    instrumenter.attachHypervolumeCollector();
                    break;
                case "generational-distance":
                    instrumenter.attachGenerationalDistanceCollector();
                    break;
                case "generational-distance-plus":
                    instrumenter.attachGenerationalDistancePlusCollector();
                    break;
                case "inverted-generational-distance":
                    instrumenter.attachInvertedGenerationalDistanceCollector();
                    break;
                case "inverted-generational-distance-plus":
                    instrumenter.attachInvertedGenerationalDistancePlusCollector();
                    break;
                case "spacing":
                    instrumenter.attachSpacingCollector();
                    break;
                case "additive-epsilon-indicator":
                    instrumenter.attachAdditiveEpsilonIndicatorCollector();
                    break;
                case "contribution":
                    instrumenter.attachContributionCollector();
                    break;
                case "maximum-pareto-front-error":
                    instrumenter.attachMaximumParetoFrontErrorCollector();
                    break;
                case "r1":
                    instrumenter.attachR1Collector();
                    break;
                case "r2":
                    instrumenter.attachR2Collector();
                    break;
                case "r3":
                    instrumenter.attachR3Collector();
                    break;
                case "epsilon-progress":
                    instrumenter.attachEpsilonProgressCollector();
                    break;
                case "adaptive-multimethod-variation":
                    instrumenter.attachAdaptiveMultimethodVariationCollector();
                    break;
                case "adaptive-time-continuation":
                    instrumenter.attachAdaptiveTimeContinuationCollector();
                    break;
                case "approximation-set":
                    instrumenter.attachApproximationSetCollector();
                    break;
                case "population":
                    instrumenter.attachPopulationCollector();
                    break;
                case "population-size":
                    instrumenter.attachPopulationSizeCollector();
                    break;
                default:
                    log.warn("Unknown metric specified: {}", metric);
            }
        }
        return instrumenter;
    }
}