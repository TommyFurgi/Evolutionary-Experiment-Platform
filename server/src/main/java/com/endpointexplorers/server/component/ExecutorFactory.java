package com.endpointexplorers.server.component;

import com.endpointexplorers.server.request.RunExperimentRequest;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExecutorFactory {

    public Executor createExecutor(RunExperimentRequest request, Instrumenter instrumenter) {
        return new Executor()
                .withProblem(request.problemName())
                .withAlgorithm(request.algorithm())
                .withMaxEvaluations(request.evaluationNumber())
                .withInstrumenter(instrumenter);
    }
}
