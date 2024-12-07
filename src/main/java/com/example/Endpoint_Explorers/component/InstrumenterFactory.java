package com.example.Endpoint_Explorers.component;

import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.Instrumenter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InstrumenterFactory {

    public Instrumenter createInstrumenter(RunExperimentRequest request) {
        return new Instrumenter()
                .withProblem(request.getProblemName())
                .withFrequency(100) //now it is fixed to 100, but we should consider passing additional  parameter
                .attachElapsedTimeCollector()
                .attachAll(); //now we attach all metrics, it should be fixed in the future
    }
}