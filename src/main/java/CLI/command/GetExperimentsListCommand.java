package CLI.command;

import CLI.config.CliConfig;
import CLI.experiment.DataPrinter;
import CLI.experiment.Experiment;
import CLI.experiment.ExperimentMapper;
import CLI.handler.GlobalExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "list", description = "Get list of experiments")
public class GetExperimentsListCommand implements Runnable {
    @CommandLine.Option(names = {"-s", "--status"}, arity = "0..*", description = "Statuses of experiments", defaultValue = "")
    private List<String> statuses;

    @CommandLine.Option(names = {"-p", "--problem"}, arity = "0..*", description = "Problem's name of experiments", defaultValue = "")
    private List<String> problems;

    @CommandLine.Option(names = {"-a", "--algorithm"}, arity = "0..*", description = "Algorithms used in experiments", defaultValue = "")
    private List<String> algorithms;

    @CommandLine.Option(names = {"-m", "--metrics"}, arity = "0..*", description = "Metrics of experiments", defaultValue = "")
    private List<String> metrics;

    @Override
    public void run() {
        String baseUrl = CliConfig.getInstance().getExperimentListUrl();

        Map<String, Object> bodyMap = new HashMap<>();
        if (statuses != null && !statuses.isEmpty() && !statuses.get(0).isEmpty()) {
            bodyMap.put("statuses", statuses);
        }

        if (problems != null && !problems.isEmpty() && !problems.get(0).isEmpty()) {
            bodyMap.put("problems", problems);
        }

        if (algorithms != null && !algorithms.isEmpty() && !algorithms.get(0).isEmpty()) {
            bodyMap.put("algorithms", algorithms);
        }

        if (metrics != null && !metrics.isEmpty() && !metrics.get(0).isEmpty()) {
            bodyMap.put("metrics", metrics);
        }

        RestTemplate restTemplate = new RestTemplate();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(bodyMap);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, requestEntity, String.class);

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
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
