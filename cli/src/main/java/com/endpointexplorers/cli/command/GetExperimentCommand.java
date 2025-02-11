package com.endpointexplorers.cli.command;

import com.endpointexplorers.cli.config.CliConfig;
import com.endpointexplorers.cli.experiment.Experiment;
import com.endpointexplorers.cli.experiment.ExperimentMapper;
import com.endpointexplorers.cli.experiment.DataPrinter;
import com.endpointexplorers.cli.experiment.ExperimentStatusEnum;
import com.endpointexplorers.cli.handler.GlobalExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;

@CommandLine.Command(name = "get", description = "Get experiment from server")
public class GetExperimentCommand implements Runnable {
    @CommandLine.Parameters(index = "0", description = "Id of experiment")
    private int experimentId;

    @Override
    public void run() {
        String url = CliConfig.GET_EXPERIMENT_URL + experimentId;
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            Experiment experiment = ExperimentMapper.parseExperiment(response);

            ExperimentStatusEnum statusEnum = ExperimentStatusEnum.valueOf(experiment.status());
            if (statusEnum == ExperimentStatusEnum.COMPLETED) {
                DataPrinter.displayTable(experiment);
            }

        } catch (HttpClientErrorException e) {
            GlobalExceptionHandler.handleHttpClientError(e, "Error while getting experiment: Experiment with id: " + experimentId + " not found");
        }
    }
}

