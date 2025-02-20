package com.endpointexplorers.cli.command;

import com.endpointexplorers.cli.handler.GlobalExceptionHandler;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;

@CommandLine.Command(name = "delete", description = "Delete experiment by ID or group name")
public class DeleteExperimentCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "ID or GROUP NAME")
    private String target;

    @CommandLine.Option(names = {"-g", "--group"}, description = "true if target is group name, default false - ID?")
    private boolean group;

    private final String deleteExperimentGroupUrl;
    private final String deleteExperimentIdUrl;

    @Inject
    public DeleteExperimentCommand(@Named("deleteExperimentGroupUrl") String deleteExperimentGroupUrl,
                                   @Named("deleteExperimentIdUrl") String deleteExperimentIdUrl) {
        this.deleteExperimentGroupUrl = deleteExperimentGroupUrl;
        this.deleteExperimentIdUrl = deleteExperimentIdUrl;
    }

    @Override
    public void run() {
        RestTemplate restTemplate = new RestTemplate();

        try {
            if (group) {
                String url = deleteExperimentGroupUrl + target;
                restTemplate.delete(url);
                System.out.println("Deleted experiments for group: " + target);
            } else {
                int experimentId = Integer.parseInt(target);
                String url = deleteExperimentIdUrl + experimentId;
                restTemplate.delete(url);
                System.out.println("Deleted experiment with ID: " + experimentId);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid experiment ID (not a number). Use --group if you want to delete by group name.");
        } catch (ResourceAccessException e) {
            GlobalExceptionHandler.handleResourceAccessError(e);
        } catch (HttpClientErrorException e) {
            GlobalExceptionHandler.handleHttpClientError(e, "Error while deleting experiment(s): ");
        }
    }
}
