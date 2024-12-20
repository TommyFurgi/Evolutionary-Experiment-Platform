package CLI.experiment;

import CLI.config.CliConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static CLI.experiment.ExperimentMapper.parseExperimentList;

public class ScheduledExperimentFetcher {
    private static final int INITIAL_DELAY = 4;
    private static final int PERIOD = 2;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final RestTemplate restTemplate = new RestTemplate();


    public void startRequesting() {
        scheduler.scheduleAtFixedRate(this::sendRequest, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);
    }

    private void sendRequest() {
        try {
            String Url = CliConfig.getInstance().getCheckStatusUrl();
            ResponseEntity<String> response = restTemplate.getForEntity(Url, String.class);
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
            });

        } else {
            System.err.println("Failed to fetch experiment. Status: " + response.getStatusCode());
        }
    }

}
