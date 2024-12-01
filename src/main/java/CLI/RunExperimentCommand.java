package CLI;

import com.example.Endpoint_Explorers.request.RunExperimentRequest;

import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

@Command(name = "run", description = "Run experiment")
public class RunExperimentCommand implements Runnable {
//    @Value("${BASE_URL}")
//    private String serverURL;

    @Parameters(index = "0", description = "Name of the experiment")
    private String problemName;

    @Parameters(index = "1", description = "Algorithm to use")
    private String algorithm;

    @CommandLine.Option(names = "-m", arity = "1..*", description = "List of metrics")
    private List<String> metrics;

    @CommandLine.Option(names = "-e", arity = "0..*", description = "List of evaluation numbers (optional)")
    private List<Integer> evaluationNumbers;

    @Override
    public void run() {
        System.out.printf("Running experiment: %s, Algorithm: %s, Metrics: %s, Evaluations: %s%n",
                problemName, algorithm, metrics, evaluationNumbers);

        String url = "http://localhost:8080/experiment/run";

        RunExperimentRequest request = new RunExperimentRequest(
                problemName, algorithm, metrics, evaluationNumbers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(url, request, String.class);
            System.out.println("Server response: " + response);
        } catch (Exception e) {
            System.err.println("Error while running experiment: " + e.getMessage());
        }
    }
}
