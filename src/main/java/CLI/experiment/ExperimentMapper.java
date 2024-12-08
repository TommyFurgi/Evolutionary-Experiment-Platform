package CLI.experiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;

public class ExperimentMapper {
    public static Experiment parseExperiment(ResponseEntity<String> response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response.getBody(), Experiment.class);
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing experiment data: " + e.getMessage());
        }
    }
}