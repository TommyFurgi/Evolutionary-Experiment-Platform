package com.endpointexplorers.cli.command;

import com.endpointexplorers.cli.config.CliDefaults;
import com.endpointexplorers.cli.component.DataPrinter;
import com.endpointexplorers.cli.experiment.Experiment;
import com.endpointexplorers.cli.experiment.ExperimentMapper;
import com.endpointexplorers.cli.handler.GlobalExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;

import java.util.*;

@CommandLine.Command(name = "list", description = "Get list of experiments")
public class GetExperimentsListCommand implements Runnable {
    @CommandLine.Option(names = {"-s", "--status"}, arity = "0..*", description = "Statuses of experiments")
    private final List<String> statuses = new ArrayList<>();

    @CommandLine.Option(names = {"-p", "--problem"}, arity = "0..*", description = "Problem's name of experiments")
    private final List<String> problems = new ArrayList<>();

    @CommandLine.Option(names = {"-a", "--algorithm"}, arity = "0..*", description = "Algorithms used in experiments")
    private final List<String> algorithms = new ArrayList<>();

    @CommandLine.Option(names = {"-m", "--metrics"}, arity = "0..*", description = "Metrics of experiments")
    private final List<String> metrics = new ArrayList<>();

    @CommandLine.Option(names = {"-g", "--groupNames"}, arity = "0..*", description = "Names of the groups")
    private final List<String> groupNames = new ArrayList<>();

    private final String experimentListUrl;

    @Inject
    public GetExperimentsListCommand(@Named("getExperimentListUrl") String experimentListUrl) {
        this.experimentListUrl = experimentListUrl;
    }

    @Override
    public void run() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("problems", problems);
        requestBody.put("algorithms", algorithms);
        requestBody.put("metrics", metrics);
        requestBody.put("statuses", statuses);
        requestBody.put("groupNames", groupNames);

        RestTemplate restTemplate = new RestTemplate();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(experimentListUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                List<Experiment> experiments = ExperimentMapper.parseExperimentList(response);
                DataPrinter.displayExperimentsList(experiments);
            } else if (response.getStatusCode().is4xxClientError()) {
                handleClientError(response);
            } else {
                System.err.println("Failed to fetch experiments. Status: " + response.getStatusCode());
            }
        } catch (ResourceAccessException e) {
            GlobalExceptionHandler.handleResourceAccessError(e);
        } catch (HttpClientErrorException e) {
            GlobalExceptionHandler.handleHttpClientError(e, "Error while getting experiments: ");
        }
    }

    private void handleClientError(ResponseEntity<String> response) {
        try {
            List<String> availableStatuses = Arrays.asList(response.getBody().split(","));

            System.err.println("Invalid status provided. Available statuses are:");
            availableStatuses.forEach(status -> System.out.println("- " + status));
        } catch (Exception e) {
            System.err.println("Failed to parse available statuses from the response.");
        }
    }
}
