package CLI.command;

import CLI.config.CliConfig;
import CLI.experiment.DataPrinter;
import CLI.experiment.Experiment;
import CLI.experiment.ExperimentMapper;
import CLI.handler.GlobalExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "list", description = "Get information about experiments with status")
public class GetExperimentsListCommand implements Runnable {
    @CommandLine.Parameters(index = "0", description = "Status of experiments", defaultValue = "all")
    private String experimentStatus;

    @Override
    public void run() {
        String url = CliConfig.getInstance().getExperimentListUrl() + experimentStatus;
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                List<Experiment> experiments = ExperimentMapper.parseExperimentList(response);
                DataPrinter.displayExperimentsList(experiments);
            } else if (response.getStatusCode().is4xxClientError()) {
                handleClientError(response);
            } else {
                System.err.println("Failed to fetch experiments. Status: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            GlobalExceptionHandler.handleHttpClientError(e, "Error while getting experiments: ");
        }
    }

    private void handleClientError(ResponseEntity<String> response) {
        try {
            List<String> availableStatuses = List.of(response.getBody().split(","));
            System.err.println("Invalid status provided. Available statuses are:");
            for (String status : availableStatuses) {
                System.out.println("- " + status);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse available statuses from the response.");
        }
    }
}
