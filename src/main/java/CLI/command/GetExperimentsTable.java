package CLI.command;

import CLI.config.CliConfig;
import CLI.experiment.Experiment;
import CLI.experiment.ExperimentMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "get-all", description = "Get information about all experiments")
public class GetExperimentsTable implements Runnable {

    @Override
    public void run() {
        String url = CliConfig.getInstance().getGetExperimentsTable();
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                List<Experiment> experiments = ExperimentMapper.parseExperimentList(response);
                displayTable(experiments);
            } else {
                System.err.println("Failed to fetch experiments. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error while getting experiments: " + e.getMessage());
        }
    }

    private void displayTable(List<Experiment> experiments) {
        System.out.printf("%-5s %-15s %-15s %-15s %-15s%n", "ID", "Evaluations", "Algorithm", "Problem", "Status");
        System.out.println("----------------------------------------------------------------------");

        for (Experiment experiment : experiments) {
            System.out.printf("%-5d %-15d %-15s %-15s %-15s%n",
                    experiment.getId(),
                    experiment.getNumberOfEvaluation(),
                    experiment.getAlgorithm(),
                    experiment.getProblemName(),
                    experiment.getStatus());
        }
    }
}
