package com.endpointexplorers.cli.component;

import com.endpointexplorers.cli.experiment.Experiment;
import com.endpointexplorers.cli.handler.GlobalExceptionHandler;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.endpointexplorers.cli.experiment.ExperimentMapper.parseExperimentList;

public class ScheduledExperimentFetcher {
    private static final int INITIAL_DELAY = 12;
    private static final int PERIOD = 4;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final RestTemplate restTemplate = new RestTemplate();
    private boolean connectionEstablished = false;
    private final String completedExperimentsUrl;

    @Inject
    public ScheduledExperimentFetcher(@Named("getCompletedExperimentsUrl") String completedExperimentsUrl) {
        this.completedExperimentsUrl = completedExperimentsUrl;
    }

    public void startRequesting() {
        scheduler.scheduleAtFixedRate(this::sendRequest, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);
    }

    private void sendRequest() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(completedExperimentsUrl, String.class);
            handleResponse(response);
            connectionEstablished = true;
        } catch (ResourceAccessException e) {
            if (connectionEstablished)
                GlobalExceptionHandler.handleResourceAccessError(e);
        } catch (Exception e) {
            System.err.println("Unable to fetch experiment data: " + e.getMessage());
        }
    }

    private void handleResponse(ResponseEntity<String> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            List<Experiment> experiments = parseExperimentList(response);
            if (!experiments.isEmpty()) {
                System.out.print("\033[s");
                System.out.println();
                experiments.forEach(experiment -> System.out.println(experiment.toString()));
                System.out.print("\033[u");
                System.out.print("> ");
                System.out.flush();
            }
        } else {
            System.err.println("Failed to fetch experiment. Status: " + response.getStatusCode());
        }
    }
}
