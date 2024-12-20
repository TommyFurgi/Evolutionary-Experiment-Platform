package CLI.command;

import CLI.config.CliConfig;
import com.example.Endpoint_Explorers.request.StatsRequest;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@CommandLine.Command(name = "getStats", description = "Get experiment stats from server")
public class GetStatsCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "Start Date and Time in YYYY-MM-DDTHH:mm format (default: 2024-01-01T00:00)", defaultValue = "2024-01-01T00:00")
    private String startDateTime;

    @CommandLine.Parameters(index = "1", description = "End Date and Time in YYYY-MM-DDTHH:mm format (default: Current Time)", defaultValue = "")

    private String endDateTime;

    @Override
    public void run() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime start;
        LocalDateTime end;

        try {
            start = LocalDateTime.parse(startDateTime, formatter);
            if (endDateTime.isEmpty()) {
                end = LocalDateTime.now();
            } else {
                end = LocalDateTime.parse(endDateTime, formatter);
            }
        } catch (DateTimeParseException e) {
            System.err.println("Error: Dates must be in YYYY-MM-DDTHH:mm format.");
            return;
        }

        if (start.isAfter(end)) {
            System.err.println("Error: Start date and time must be earlier than or equal to end date and time.");
            return;
        }

        Timestamp startTimestamp = Timestamp.valueOf(start);
        Timestamp endTimestamp = Timestamp.valueOf(end);

        StatsRequest request = new StatsRequest(startTimestamp, endTimestamp);

        String statsUrl = CliConfig.getInstance().getStatsUrl();

        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(statsUrl, request, String.class);

            System.out.println("Server response: " + response);
        } catch (Exception e) {
            System.err.println("Failed to fetch stats: " + e.getMessage());
        }
    }
}


