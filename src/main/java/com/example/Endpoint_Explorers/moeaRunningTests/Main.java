package com.example.Endpoint_Explorers.moeaRunningTests;

import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Observations;
import org.moeaframework.core.NondominatedPopulation;



public class Main {
    public static void main(String[] args) {
        NondominatedPopulation result = new Executor()
                .withProblem("UF1")
                .withAlgorithm("NSGAII")
                .withMaxEvaluations(10000)
                .run();

        //result.display();

        Executor executor = new Executor()
                .withProblem("UF1")
                .withAlgorithm("NSGAII")
                .withMaxEvaluations(10000);

        Analyzer analyzer = new Analyzer()
                .withSameProblemAs(executor)
                .includeAllMetrics();

        analyzer.addAll("NSGAII", executor.runSeeds(50));
        analyzer.display();

        Executor executor2 = new Executor()
                .withProblem("DTLZ3")
                .withAlgorithm("eMOEA")
                .withMaxEvaluations(10000);

        Analyzer analyzer2 = new Analyzer()
                .withSameProblemAs(executor2)
                .includeAllMetrics();

        analyzer2.addAll("eMOEA", executor2.runSeeds(40));
        analyzer2.display();

        Instrumenter instrumenter = new Instrumenter()
                .withProblem("UF1")
                .withFrequency(100)
                .attachAll();

        new Executor()
                .withProblem("UF1")
                .withAlgorithm("NSGAII")
                .withMaxEvaluations(10000)
                .withInstrumenter(instrumenter)
                .run();

        instrumenter.getObservations().display();
        Observations observations = instrumenter.getObservations();
    }
}
