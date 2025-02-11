package com.endpointexplorers.cli.command;

import com.endpointexplorers.cli.config.CliConfig;
import com.endpointexplorers.cli.config.CliDefaults;
import com.endpointexplorers.cli.handler.GlobalExceptionHandler;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "setGroup", description = "Set group for experiments")
public class SetExperimentsGroupCommand implements Runnable {

    @CommandLine.Parameters(
            index = "0",
            arity = "1..*",
            description = "List of experiment IDs to update"
    )
    private List<Integer> experiments;

    @CommandLine.Option(
            names = {"-g", "--groupName"},
            arity = "1",
            description = "New group name to assign to the experiments (default: none)",
            defaultValue = CliDefaults.DEFAULT_GROUP_VALUE)
    private String groupName;

    @Override
    public void run() {
        String url = CliConfig.SET_GROUP_NAME;

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("experimentIds", experiments);
            requestBody.put("newGroupName", groupName);

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    new HttpEntity<>(requestBody),
                    String.class
            );

            System.out.println("Server response: " + response);
        } catch (HttpClientErrorException e) {
            GlobalExceptionHandler.handleHttpClientError(e, "Error while updating group for experiments: ");
        }
    }
}

