package com.example.Endpoint_Explorers.component;

import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExecutorFactory {

    public Executor createExecutor(RunExperimentRequest request, Instrumenter instrumenter) {
        return new Executor()
                .withProblem(request.getProblemName())
                .withAlgorithm(request.getAlgorithm())
                .withMaxEvaluations(request.getEvaluationNumbers())
                .withInstrumenter(instrumenter);
    }
}
