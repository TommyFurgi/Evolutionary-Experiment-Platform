package CLI.command;

import CLI.config.CliConfig;
import CLI.experiment.Experiment;
import CLI.experiment.ExperimentMapper;
import CLI.experiment.ExperimentTable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;

@CommandLine.Command(name = "get", description = "Get experiment from server")
public class GetExperimentCommand implements Runnable {
    @CommandLine.Parameters(index = "0", description = "Id of experiment")
    private int experimentId;

    @Override
    public void run() {
        String url = CliConfig.getInstance().getGetExperimentUrl() + experimentId;
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                Experiment experiment = ExperimentMapper.parseExperiment(response);

                System.out.println(experiment.toString());
                if (experiment.status().equals("COMPLETED"))
                    ExperimentTable.displayTable(experiment);
            } else {
                System.err.println("Failed to fetch experiment. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error while getting experiment: " + e.getMessage());
        }
    }
}

