package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.component.CsvContentConverter;
import com.example.Endpoint_Explorers.model.FileDetails;
import com.example.Endpoint_Explorers.utils.DirectoryUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CsvService {

    private static final String BASE_PATH = "src/main/java/com/example/Endpoint_Explorers/serverResources/csv/";

    public FileDetails createCsv(
            Map<String, List<Double>> metricsResults,
            String problemName,
            String algorithm,
            String startDateTime,
            String endDateTime
    ) {
        String csvContent = CsvContentConverter.buildCsvContent(metricsResults);

        String safeStartDateTime = startDateTime.replace(":", "-");
        String safeEndDateTime = endDateTime.replace(":", "-");
        String fileName = "stats_"
                + problemName + "_" + algorithm + "_"
                + safeStartDateTime + "_" + safeEndDateTime
                + ".csv";

        DirectoryUtils.ensureDirectoryExists(BASE_PATH);
        String fullPath = BASE_PATH + fileName;

        try (FileWriter writer = new FileWriter(fullPath)) {
            writer.write(csvContent);
            log.info("CSV saved successfully at: " + fullPath);
        } catch (IOException e) {
            throw new RuntimeException("Error while saving CSV file: " + e.getMessage(), e);
        }

        byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);
        String base64Encoded = Base64.getEncoder().encodeToString(csvBytes);

        return new FileDetails(fileName, base64Encoded);
    }
}
