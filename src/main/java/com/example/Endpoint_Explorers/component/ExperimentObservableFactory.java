package com.example.Endpoint_Explorers.component;

import com.example.Endpoint_Explorers.request.RunExperimentRequest;
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

    private final InstrumenterFactory instrumenterFactory;
    private final ExecutorFactory executorFactory;

    public Observable<Observations> createExperimentObservable(RunExperimentRequest request) throws IllegalArgumentException {
        return Observable.fromCallable(() -> {
            Instrumenter instrumenter = instrumenterFactory.createInstrumenter(request);
            Executor executor = executorFactory.createExecutor(request, instrumenter);
            executor.run();
            return instrumenter.getObservations();
        });
    }
}
