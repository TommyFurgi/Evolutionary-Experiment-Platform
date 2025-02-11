package com.endpointexplorers.cli.command;

import com.endpointexplorers.cli.config.CliConfig;
import com.endpointexplorers.cli.config.CliDefaults;
import com.endpointexplorers.cli.handler.GlobalExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;
import picocli.CommandLine.Command;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Command(name = "runManyDiff", description = "Run many different experiments for multiple problems/algorithms")
public class RunManyDifferentExperimentCommand implements Runnable {

    @CommandLine.Option(names = {"-p", "--problems"}, arity = "1..*", description = "List of problems", required = true)
    private List<String> problems;

    @CommandLine.Option(names = {"-a", "--algorithms"}, arity = "1..*", description = "List of algorithms", required = true)
    private List<String> algorithms;

    @CommandLine.Option(names = {"-m", "--metrics"}, arity = "1..*", description = "List of metrics to evaluate", defaultValue = CliDefaults.DEFAULT_METRIC_GENERAL)
    private List<String> metrics;

    @CommandLine.Option(names = {"-e", "--evaluations"}, defaultValue = CliDefaults.DEFAULT_EVALUATION_NUMBER)
    private Integer evaluationNumber;

    @CommandLine.Option(names = {"-n", "--experimentIterationNumber"}, defaultValue = CliDefaults.DEFAULT_EXPERIMENT_ITERATION_NUMBER)
    private Integer experimentIterationNumber;

    @CommandLine.Option(names = {"-g", "--groupName"}, description = "Name of the group (default: none)", defaultValue = CliDefaults.DEFAULT_GROUP_VALUE)
    private String groupName;

    @Override
    public void run() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("problems", problems);
        requestBody.put("algorithms", algorithms);
        requestBody.put("metrics", metrics);
        requestBody.put("evaluationNumber", evaluationNumber);
        requestBody.put("experimentIterationNumber", experimentIterationNumber);
        requestBody.put("groupName", groupName);

        RestTemplate restTemplate = new RestTemplate();
        String url = CliConfig.RUN_MANY_DIFFERENT_EXPERIMENTS_URL;

        System.out.println("Preparing to run many-different-experiments ...");
        try {
            String response = restTemplate.postForObject(url, requestBody, String.class);
            System.out.println("Server response: " + response);
        } catch (HttpClientErrorException e) {
            GlobalExceptionHandler.handleHttpClientError(e, "Error while running many-different-experiments: ");
        }
    }
}
