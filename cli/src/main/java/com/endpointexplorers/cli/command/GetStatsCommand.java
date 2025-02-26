package com.endpointexplorers.cli.command;

import com.endpointexplorers.cli.component.FilesSaver;
import com.endpointexplorers.cli.component.MetricsAndFiles;
import com.endpointexplorers.cli.config.CliDefaults;
import com.endpointexplorers.cli.component.DataPrinter;
import com.endpointexplorers.cli.handler.GlobalExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import picocli.CommandLine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "getStats", description = "Get experiment stats from server")
public class GetStatsCommand implements Runnable {
    private record LocalDateTimeResult(LocalDateTime start, LocalDateTime end) {}
    private final FilesSaver filesSaver;

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

    @CommandLine.Option(
            names = {"-a", "--statType"},
            description = "Type of Statistics measure (default: median)",
            defaultValue = CliDefaults.DEFAULT_STATISTIC_TYPE)
    private String statType;

    @CommandLine.Option(
            names = {"-p", "--plot"},
            description = "Plot metrics (true if specified, false otherwise)"
    )
    private boolean isPlot;

    @CommandLine.Option(
            names = {"-c", "--csv"},
            description = "Save data to CSV (true if specified, false otherwise)"
    )
    private boolean isCsv;

    @CommandLine.Option(
            names = {"-m", "--metricsNameToPlot"},
            description = "Specify one, multiple metric names, or use 'all' to select all metrics.",
            defaultValue = CliDefaults.DEFAULT_METRIC_NAMES,
            arity = "1..*"
    )
    private List<String> metricsNamesToPlot;

    @CommandLine.Option(
            names = {"-g", "--groupName"},
            description = "Name of the group (default: none)",
            defaultValue = CliDefaults.DEFAULT_GROUP_VALUE)
    private String groupName;

    private final String statsUrl;

    @Inject
    public GetStatsCommand(@Named("getStatsUrl") String statsUrl, FilesSaver filesSaver) {
        this.statsUrl = statsUrl;
        this.filesSaver = filesSaver;
    }

    @Override
    public void run() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

        LocalDateTimeResult result = getLocalDateTimeResult(formatter);
        if (result == null) return;

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("problemName", problemName);
        requestBody.put("algorithm", algorithm);
        requestBody.put("startDateTime", result.start().format(formatter));
        requestBody.put("endDateTime", result.end().format(formatter));
        requestBody.put("statType", statType);
        requestBody.put("isPlot", true);
        requestBody.put("isCsv", isCsv);
        requestBody.put("metricsNamesToPlot", metricsNamesToPlot);
        requestBody.put("groupName", groupName);

        calculateStatsUsingServer(restTemplate, statsUrl, requestBody);
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

    private void calculateStatsUsingServer(RestTemplate restTemplate, String finalUrl, Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            String response = restTemplate.exchange(finalUrl, HttpMethod.POST, requestEntity, String.class).getBody();
            ObjectMapper objectMapper = new ObjectMapper();

            MetricsAndFiles metricsAndFiles = objectMapper.readValue(response, new TypeReference<>() {});

            DataPrinter.printStats(
                    problemName,
                    algorithm,
                    startDateTime,
                    endDateTime,
                    statType,
                    metricsAndFiles.getMetrics(),
                    groupName
            );

            if (!metricsNamesToPlot.get(0).equals(CliDefaults.DEFAULT_METRIC_NAMES) || isCsv) {
                filesSaver.saveFiles(metricsAndFiles.getFiles());
            }
        } catch (ResourceAccessException e) {
            GlobalExceptionHandler.handleResourceAccessError(e);
        } catch (HttpClientErrorException e) {
            GlobalExceptionHandler.handleHttpClientError(e, "Failed to fetch statistics: ");
        } catch (JsonMappingException e) {
            GlobalExceptionHandler.handleJsonMappingError(e);
        } catch (JsonProcessingException e) {
            GlobalExceptionHandler.handleJsonProcessingError(e);
        }
    }
}