package CLI;

import com.example.Endpoint_Explorers.request.RunExperimentRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

@Command(name = "run", description = "Run an experiment on the server")
public class RunExperimentCommand implements Runnable {

    @Parameters(index = "0", description = "Name of the problem to solve")
    private String problemName;

    @Parameters(index = "1", description = "Algorithm to use for solving the problem")
    private String algorithm;

    @CommandLine.Option(names = {"-m", "--metrics"}, arity = "1..*", description = "List of metrics to evaluate")
    private List<String> metrics;

    @CommandLine.Option(names = {"-e", "--evaluations"}, description = "Number of evaluations (default: 10000)")
    private Integer evaluationNumber;

    @Override
    public void run() {
        System.out.printf("Preparing to run experiment:%n Problem: %s%n Algorithm: %s%n Metrics: %s%n Evaluations: %d%n",
                problemName, algorithm, metrics, evaluationNumber);

        String url = "http://localhost:8080/experiment/run";

        RunExperimentRequest request = new RunExperimentRequest(
                problemName, algorithm, metrics, evaluationNumber);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(url, request, String.class);
            System.out.println("Server response: " + response);
        } catch (Exception e) {
            System.err.println("Error while running experiment: " + e.getMessage());
        }
    }
}
