package com.endpointexplorers.cli.command;

import com.endpointexplorers.cli.config.CliConfig;
import com.endpointexplorers.cli.config.CliDefaults;
import com.endpointexplorers.cli.handler.GlobalExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Command(name = "run", description = "Run an experiment on the server ( run UF1 e-MOEA )")
public class RunExperimentCommand implements Runnable {

    @Parameters(index = "0", description = "Name of the problem to solve")
    private String problemName;

    @Parameters(index = "1", description = "Algorithm to use for solving the problem")
    private String algorithm;

    @CommandLine.Option(names = {"-m", "--metrics"}, arity = "1..*", description = "List of metrics to evaluate", defaultValue = CliDefaults.DEFAULT_METRIC_GENERAL)
    private List<String> metrics;

    @CommandLine.Option(names = {"-e", "--evaluations"}, description = "Number of evaluations (default: 10000)", defaultValue = CliDefaults.DEFAULT_EVALUATION_NUMBER)
    private Integer evaluationNumber;

    @CommandLine.Option(names = {"-n", "--experimentIterationNumber"}, description = "Number of experiment iteration (default: 1)", defaultValue = CliDefaults.DEFAULT_EXPERIMENT_ITERATION_NUMBER)
    private Integer experimentIterationNumber;

    @CommandLine.Option(names = {"-g", "--groupName"}, description = "Name of the group (default: none)", defaultValue = CliDefaults.DEFAULT_GROUP_VALUE)
    private String groupName;

    @Override
    public void run() {
        String urlExperiment = CliConfig.RUN_EXPERIMENT_URL;
        String urlExperiments = CliConfig.RUN_EXPERIMENTS_URL;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("problemName", problemName);
        requestBody.put("algorithm", algorithm);
        requestBody.put("metrics", metrics);
        requestBody.put("evaluationNumber", evaluationNumber);
        requestBody.put("experimentIterationNumber", experimentIterationNumber);
        requestBody.put("groupName", groupName);

        RestTemplate restTemplate = new RestTemplate();

        if (experimentIterationNumber == 1) {
            System.out.printf("Preparing to run experiment:%n Problem: %s%n Algorithm: %s%n Metrics: %s%n Evaluations: %d%n Group: %s%n",
                    problemName, algorithm, metrics, evaluationNumber, groupName);
            try {
                String response = restTemplate.postForObject(urlExperiment, requestBody, String.class);
                System.out.println("Server response: " + response);
            } catch (HttpClientErrorException e) {
                GlobalExceptionHandler.handleHttpClientError(e, "Error while running experiment: ");
            }
        } else if (experimentIterationNumber > 1) {
            System.out.printf("Preparing to run %d experiments:%n Problem: %s%n Algorithm: %s%n Metrics: %s%n Evaluations: %d%n Group: %s%n",
                    experimentIterationNumber, problemName, algorithm, metrics, evaluationNumber, groupName);
            try {
                String response = restTemplate.postForObject(urlExperiments, requestBody, String.class);
                System.out.println("Server response: " + response);
            } catch (HttpClientErrorException e) {
                GlobalExceptionHandler.handleHttpClientError(e, "Error while running experiments: ");
            }
        }
    }
}
