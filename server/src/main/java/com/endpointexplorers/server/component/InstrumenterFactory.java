package com.endpointexplorers.server.component;

import com.endpointexplorers.server.model.MetricTypeEnum;
import com.endpointexplorers.server.request.RunExperimentRequest;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.Instrumenter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class InstrumenterFactory {

    public Instrumenter createInstrumenter(RunExperimentRequest request, int frequency) {
        Instrumenter instrumenter = new Instrumenter()
                .withProblem(request.getProblemName())
                .withFrequency(frequency);

        for (String metricName : request.getMetrics()) {
            Optional<MetricTypeEnum> metricOpt = MetricTypeEnum.fromString(metricName);
            if (metricOpt.isPresent()) {
                metricOpt.get().attach(instrumenter);
            } else {
                log.warn("Unknown metric specified: {}", metricName);
            }
        }
        return instrumenter;
    }
}