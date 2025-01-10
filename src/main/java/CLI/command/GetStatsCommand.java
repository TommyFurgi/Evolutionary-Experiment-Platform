package CLI.command;

import CLI.config.CliConfig;
import CLI.config.CliDefaults;
import CLI.experiment.DataPrinter;
import CLI.handler.GlobalExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.HttpClientErrorException;
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
    private record LocalDateTimeResult(LocalDateTime start, LocalDateTime end) {
    }

    @CommandLine.Parameters(index = "0", description = "Calculate stats for the given problem")
    private String problemName;

    @CommandLine.Parameters(index = "1", description = "Calculate stats for the given algorithm")
    private String algorithm;

    @CommandLine.Option(
            names = {"-s", "--start"},
            description = "Start Date and Time in yyyy-MM-dd HH:mm format (default: 2024-01-01 00:00:00)",
            defaultValue = CliDefaults.DEFAULT_START_DATE
    )
    private String startDateTime;

    @CommandLine.Option(
            names = {"-e", "--end"},
            description = "End Date and Time in yyyy-MM-dd HH:mm format (default: Current Time)",
            defaultValue = CliDefaults.DEFAULT_END_DATE
    )
    private String endDateTime;

    @CommandLine.Option(names = {"-a", "--statType"}, description = "Type of Statistics measure (default: median)", defaultValue = CliDefaults.DEFAULT_STATISTIC_TYPE)
    private String statType;

    @Override
    public void run() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

        LocalDateTimeResult result = getLocalDateTimeResult(formatter);
        if (result == null) return;

        RestTemplate restTemplate = new RestTemplate();
        String statsUrl = CliConfig.STATS_URL;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(statsUrl)
                .queryParam("problemName", problemName)
                .queryParam("algorithm", algorithm)
                .queryParam("startDateTime", result.start().format(formatter))
                .queryParam("endDateTime", result.end().format(formatter))
                .queryParam("statType", statType);

        String finalUrl = builder.build().toUriString();
        calculateStatsUsingServer(restTemplate, finalUrl);
    }

    private LocalDateTimeResult getLocalDateTimeResult(DateTimeFormatter formatter) {
        LocalDateTime start;
        LocalDateTime end;
        try {
            start = LocalDateTime.parse(startDateTime, formatter);
            if (endDateTime.isEmpty()) {
                end = LocalDateTime.now();
            } else {
                end = LocalDateTime.parse(endDateTime, formatter);
            }
            System.out.println(start);
            System.out.println(end);
        } catch (DateTimeParseException e) {
            System.err.println("Error: Dates must be in 'yyyy-MM-dd HH:mm' format.");
            return null;
        }

        if (start.isAfter(end)) {
            System.err.println("Error: Start date and time must be earlier than or equal to end date and time.");
            return null;
        }
        return new LocalDateTimeResult(start, end);
    }

    private void calculateStatsUsingServer(RestTemplate restTemplate, String finalUrl) {
        try {
            String response = restTemplate.getForObject(finalUrl, String.class);
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, List<Double>> metricsMap = objectMapper.readValue(response, new TypeReference<>() {
            });
            DataPrinter.printStats(problemName, algorithm, startDateTime, endDateTime, statType, metricsMap);

        } catch (HttpClientErrorException e) {
            GlobalExceptionHandler.handleHttpClientError(e, "Failed to fetch statistics: ");
        } catch (JsonMappingException e) {
            GlobalExceptionHandler.handleJsonMappingError(e);
        } catch (JsonProcessingException e) {
            GlobalExceptionHandler.handleJsonProcessingError(e);
        }
    }

}