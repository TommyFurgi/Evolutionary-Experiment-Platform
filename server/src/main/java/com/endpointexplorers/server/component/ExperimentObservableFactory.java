package com.endpointexplorers.server.component;

import com.endpointexplorers.server.request.RunExperimentsRequest;
import io.reactivex.rxjava3.core.Observable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Observations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExperimentObservableFactory {
    private static final int FREQUENCY = 100;
    private final InstrumenterFactory instrumenterFactory;
    private final ExecutorFactory executorFactory;

    public static int getFrequency() {
        return FREQUENCY;
    }

    public Observable<Observations> createExperimentObservable(RunExperimentsRequest request) throws IllegalArgumentException {
        return Observable.fromCallable(() -> {
            Instrumenter instrumenter = instrumenterFactory.createInstrumenter(request, FREQUENCY);
            Executor executor = executorFactory.createExecutor(request, instrumenter);
            executor.run();
            return instrumenter.getObservations();
        });
    }
}
