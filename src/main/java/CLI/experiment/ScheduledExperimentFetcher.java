package CLI.experiment;

import CLI.experiment.Experiment;
import CLI.experiment.ExperimentTable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledExperimentFetcher {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String URL = "http://localhost:8080/experiment/ready";
    private static final int INITIAL_DELAY = 0;
    private static final int PERIOD = 2;

    public void startRequesting() {
        scheduler.scheduleAtFixedRate(this::sendRequest, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);
    }

    private void sendRequest() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(URL, String.class);
            handleResponse(response);
        } catch (Exception e) {
            System.err.println("Error while getting experiment: " + e.getMessage());
        }
    }

    private void handleResponse(ResponseEntity<String> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            List<Experiment> experiments = parseExperimentList(response);
            experiments.forEach(experiment -> {
                System.out.println(experiment.toString());
                ExperimentTable.displayTable(experiment);
            });

        } else {
            System.err.println("Failed to fetch experiment. Status: " + response.getStatusCode());
        }
    }

    private List<Experiment> parseExperimentList(ResponseEntity<String> response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response.getBody(), new TypeReference<List<Experiment>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing experiment list: " + e.getMessage());
        }
    }
}
