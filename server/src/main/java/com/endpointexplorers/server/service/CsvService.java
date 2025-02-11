package com.endpointexplorers.server.service;

import com.endpointexplorers.server.component.CsvContentConverter;
import com.endpointexplorers.server.model.FileDetails;
import com.endpointexplorers.server.utils.DirectoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvService {

    @Value("${path.serverCSVsResources}")
    private String serverResourcesPath;
    private final CsvContentConverter csvContentConverter;

    public FileDetails createCsv(
            Map<String, List<Double>> metricsResults,
            String problemName,
            String algorithm
    ) {
        String csvContent = csvContentConverter.buildCsvContent(metricsResults);

        String fileName = createFileName(algorithm, problemName);
        String fullPath = createFinalPath(fileName);

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

    private String createFinalPath(String fileName) {
        DirectoryUtils.ensureDirectoryExists(serverResourcesPath);

        return serverResourcesPath + "/" + fileName;
    }

    private String createFileName(String algorithmName, String problemName) {
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String date = currentDate.format(formatter);

        return date + "-" + algorithmName + "-" + problemName + ".csv";
    }
}
