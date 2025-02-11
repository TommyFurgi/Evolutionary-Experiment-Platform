package com.endpointexplorers.cli.command;

import com.endpointexplorers.cli.config.CliConfig;
import com.endpointexplorers.cli.handler.GlobalExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;

@CommandLine.Command(name = "delete", description = "Delete experiment by ID or group name")
public class DeleteExperimentCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "ID or GROUP NAME")
    private String target;

    @CommandLine.Option(names = {"-g", "--group"}, description = "true if target is group name, default false - ID?")
    private boolean group;

    @Override
    public void run() {
        RestTemplate restTemplate = new RestTemplate();

        try {
            if (group) {
                String url = CliConfig.DELETE_EXPERIMENT_GROUP + target;
                restTemplate.delete(url);
                System.out.println("Deleted experiments for group: " + target);
            } else {
                int experimentId = Integer.parseInt(target);
                String url = CliConfig.DELETE_EXPERIMENT_ID + experimentId;
                restTemplate.delete(url);
                System.out.println("Deleted experiment with ID: " + experimentId);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid experiment ID (not a number). Use --group if you want to delete by group name.");
        } catch (HttpClientErrorException e) {
            GlobalExceptionHandler.handleHttpClientError(e, "Error while deleting experiment(s): ");
        }
    }
}
