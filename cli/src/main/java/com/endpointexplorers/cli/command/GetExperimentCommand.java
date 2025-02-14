package com.endpointexplorers.cli.command;

import com.endpointexplorers.cli.experiment.Experiment;
import com.endpointexplorers.cli.experiment.ExperimentMapper;
import com.endpointexplorers.cli.component.DataPrinter;
import com.endpointexplorers.cli.experiment.ExperimentStatusEnum;
import com.endpointexplorers.cli.handler.GlobalExceptionHandler;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;

@CommandLine.Command(name = "get", description = "Get experiment from server")
public class GetExperimentCommand implements Runnable {
    @CommandLine.Parameters(index = "0", description = "Id of experiment")
    private int experimentId;

    private final String experimentUrl;

    @Inject
    public GetExperimentCommand(@Named("getExperimentUrl") String experimentUrl) {
        this.experimentUrl = experimentUrl;
    }

    @Override
    public void run() {
        String url = experimentUrl + experimentId;
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            Experiment experiment = ExperimentMapper.parseExperiment(response);

            ExperimentStatusEnum statusEnum = ExperimentStatusEnum.valueOf(experiment.status());
            if (statusEnum == ExperimentStatusEnum.COMPLETED) {
                DataPrinter.printExperimentValues(experiment);
            }
        } catch (ResourceAccessException e) {
            GlobalExceptionHandler.handleResourceAccessError(e);
        } catch (HttpClientErrorException e) {
            GlobalExceptionHandler.handleHttpClientError(e, "Error while getting experiment: Experiment with id: " + experimentId + " not found");
        }
    }
}

