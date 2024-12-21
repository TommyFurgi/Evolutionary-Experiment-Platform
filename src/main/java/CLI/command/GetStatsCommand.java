package CLI.command;

import CLI.config.CliConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import picocli.CommandLine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "getStats", description = "Get experiment stats from server")
public class GetStatsCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "Calculate stats for the given problem")
    private String problemName;

    @CommandLine.Parameters(index = "1", description = "Calculate stats for the given algorithm")
    private String algorithm;

    @CommandLine.Parameters(
            index = "2",
            description = "Start Date and Time in yyyy-MM-dd HH:mm format (default: 2024-01-01 00:00:00)",
            defaultValue = "2024-01-01 00:00:00"
    )
    private String startDateTime;

    @CommandLine.Parameters(
            index = "3",
            description = "End Date and Time in yyyy-MM-dd HH:mm format (default: Current Time)",
            defaultValue = ""
    )
    private String endDateTime;

    @Override
    public void run() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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
            System.err.println("Error: Dates must be in 'yyyy-MM-dd HH:mm' format.");
            return;
        }

        if (start.isAfter(end)) {
            System.err.println("Error: Start date and time must be earlier than or equal to end date and time.");
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        String statsUrl = CliConfig.getInstance().getStatsUrl();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(statsUrl)
                .queryParam("problemName", problemName)
                .queryParam("algorithm", algorithm)
                .queryParam("startDateTime", start.format(formatter))
                .queryParam("endDateTime", end.format(formatter));

        String finalUrl = builder.build().toUriString();

        try {
            String response = restTemplate.getForObject(finalUrl, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, List<Double>> metricsMap = objectMapper.readValue(response, new TypeReference<>() {
            });
            printMetrics(metricsMap);

        } catch (Exception e) {
            System.err.println("Failed to fetch stats: " + e.getMessage());
        }
    }

    public void printMetrics(Map<String, List<Double>> metricsMap) {
        metricsMap.forEach((key, values) -> {
            System.out.println("MetricsName: " + key);
            if (values == null || values.isEmpty()) {
                System.out.println("Brak wartości.");
            } else {
                String formattedValues = values.stream()
                        .map(value -> String.format("%.2f", value))
                        .collect(java.util.stream.Collectors.joining(", "));
                System.out.println("  AvgValues: [" + formattedValues + "]");
            }
            System.out.println("-----------------------------------");
        });
    }
}


