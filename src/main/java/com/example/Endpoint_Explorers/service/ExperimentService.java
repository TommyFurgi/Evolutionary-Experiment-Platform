package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.component.ExperimentObservableFactory;
import com.example.Endpoint_Explorers.repository.ExperimentRepository;
import com.example.Endpoint_Explorers.request.RunExperimentRequest;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.analysis.collector.Observations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentService {
    private final ExperimentRepository repository;
    private final ExperimentObservableFactory observableFactory;

    public void runExperiment(RunExperimentRequest request) {
        log.info("Running " + request.toString());

        Observable<Observations> observable = observableFactory.createExperimentObservable(request);

        observable
                .subscribeOn(Schedulers.computation())
                .subscribe(
                        result -> handleSuccess(result, request),
                        this::handleError
                );
    }

    private void handleSuccess(Observations result, RunExperimentRequest request) {
        log.info("Experiment completed successfully for problem: {}", request.getProblemName());
        result.display();
        // here we should save results to repository and notify client instead of printing results
    }

    private void handleError(Throwable throwable) {
        log.error("Experiment execution failed", throwable);
    }
}
