package com.endpointexplorers.cli.command;

import com.endpointexplorers.cli.config.CliConfig;
import com.endpointexplorers.cli.config.CliDefaults;
import com.endpointexplorers.cli.experiment.DataPrinter;
import com.endpointexplorers.cli.experiment.Experiment;
import com.endpointexplorers.cli.experiment.ExperimentMapper;
import com.endpointexplorers.cli.handler.GlobalExceptionHandler;
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
    @CommandLine.Option(names = {"-s", "--status"}, arity = "0..*", description = "Statuses of experiments", defaultValue = CliDefaults.DEFAULT_STATUS)
    private List<String> statuses;

    @CommandLine.Option(names = {"-p", "--problem"}, arity = "0..*", description = "Problem's name of experiments", defaultValue = CliDefaults.DEFAULT_PROBLEM)
    private List<String> problems;

    @CommandLine.Option(names = {"-a", "--algorithm"}, arity = "0..*", description = "Algorithms used in experiments", defaultValue = CliDefaults.DEFAULT_ALGORITHM)
    private List<String> algorithms;

    @CommandLine.Option(names = {"-m", "--metrics"}, arity = "0..*", description = "Metrics of experiments", defaultValue = CliDefaults.DEFAULT_METRIC_NONE)
    private List<String> metrics;

    @CommandLine.Option(names = {"-g", "--groupNames"}, arity = "0..*", description = "Names of the groups", defaultValue = CliDefaults.DEFAULT_GROUP_VALUE)
    private List<String> groupNames;

    @Override
    public void run() {
        String baseUrl = CliConfig.EXPERIMENT_LIST_URL;

        Map<String, Object> bodyMap = new HashMap<>();
        addToMapIfNotDefault(bodyMap, "statuses", statuses, CliDefaults.DEFAULT_STATUS);
        addToMapIfNotDefault(bodyMap, "problems", problems, CliDefaults.DEFAULT_PROBLEM);
        addToMapIfNotDefault(bodyMap, "algorithms", algorithms, CliDefaults.DEFAULT_ALGORITHM);
        addToMapIfNotDefault(bodyMap, "metrics", metrics, CliDefaults.DEFAULT_METRIC_NONE);
        addToMapIfNotDefault(bodyMap, "groupNames", groupNames, CliDefaults.DEFAULT_GROUP_VALUE);

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

    private void addToMapIfNotDefault(Map<String, Object> bodyMap, String key, List<String> values, String defaultValue) {
        if (values != null && !values.isEmpty() && !values.get(0).equals(defaultValue)) {
            bodyMap.put(key, values);
        }
    }
}
