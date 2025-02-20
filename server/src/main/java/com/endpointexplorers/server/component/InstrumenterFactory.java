package com.endpointexplorers.server.component;

import com.endpointexplorers.server.model.MetricTypeEnum;
import com.endpointexplorers.server.request.RunExperimentsRequest;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.Instrumenter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class InstrumenterFactory {

    public Instrumenter createInstrumenter(RunExperimentsRequest request, int frequency) {
        Instrumenter instrumenter = new Instrumenter()
                .withProblem(request.problemName())
                .withFrequency(frequency);

        for (String metricName : request.metrics()) {
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