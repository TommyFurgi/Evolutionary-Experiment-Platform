package com.endpointexplorers.cli.command;

import com.endpointexplorers.cli.config.CliDefaults;
import com.endpointexplorers.cli.handler.GlobalExceptionHandler;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;
import picocli.CommandLine.Command;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Command(name = "runManyDiff", description = "Run many different experiments for multiple problems/algorithms")
public class RunMultipleExperimentsCommand implements Runnable {

    @CommandLine.Option(names = {"-p", "--problems"}, arity = "1..*", description = "List of problems", required = true)
    private List<String> problems;

    @CommandLine.Option(names = {"-a", "--algorithms"}, arity = "1..*", description = "List of algorithms", required = true)
    private List<String> algorithms;

    @CommandLine.Option(names = {"-m", "--metrics"}, arity = "1..*", description = "List of metrics to evaluate", defaultValue = CliDefaults.DEFAULT_METRIC_GENERAL)
    private List<String> metrics;

    @CommandLine.Option(names = {"-e", "--evaluations"}, defaultValue = CliDefaults.DEFAULT_EVALUATION_NUMBER)
    private Integer evaluationNumber;

    @CommandLine.Option(names = {"-n", "--experimentIterationNumber"}, defaultValue = CliDefaults.DEFAULT_EXPERIMENTS_NUMBER)
    private Integer experimentsNumber;

    @CommandLine.Option(names = {"-g", "--groupName"}, description = "Name of the group (default: none)", defaultValue = CliDefaults.DEFAULT_GROUP_VALUE)
    private String groupName;

    private final String runMultipleExperimentsUrl;

    @Inject
    public RunMultipleExperimentsCommand(@Named("runMultipleExperimentsUrl") String runMultipleExperimentsUrl) {
        this.runMultipleExperimentsUrl = runMultipleExperimentsUrl;
    }

    @Override
    public void run() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("problems", problems);
        requestBody.put("algorithms", algorithms);
        requestBody.put("metrics", metrics);
        requestBody.put("evaluationNumber", evaluationNumber);
        requestBody.put("experimentsNumber", experimentsNumber);
        requestBody.put("groupName", groupName);

        RestTemplate restTemplate = new RestTemplate();

        System.out.println("Preparing to run many-different-experiments ...");
        try {
            String response = restTemplate.postForObject(runMultipleExperimentsUrl, requestBody, String.class);
            System.out.println("Server response: " + response);
        } catch (ResourceAccessException e) {
            GlobalExceptionHandler.handleResourceAccessError(e);
        } catch (HttpClientErrorException e) {
            GlobalExceptionHandler.handleHttpClientError(e, "Error while running many-different-experiments: ");
        }
    }
}
