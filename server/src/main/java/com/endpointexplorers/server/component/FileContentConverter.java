package com.endpointexplorers.server.component;

import com.endpointexplorers.server.model.FileDetails;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FileContentConverter {
    private static final Path BASE_PATH = Paths.get("src/main/java/com/example/Endpoint_Explorers/serverResources/plots");

    public static List<FileDetails> createFilesDetails(List<String> fileNamePathsList) {
        List<FileDetails> fileDetailsList = new ArrayList<>();
        for (String fullFilePath : fileNamePathsList) {
            Path fullPath = Paths.get(fullFilePath);
            File file = new File(fullFilePath);
            if (file.exists() && file.isFile()) {
                try {
                    Path relativePath = BASE_PATH.relativize(fullPath);
                    String contentBase64 = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(file));
                    fileDetailsList.add(new FileDetails(relativePath.toString(), contentBase64));
                } catch (Exception e) {
                    System.err.println("Error processing file: " + file.getName());
                    e.printStackTrace();
                }
            } else {
                System.err.println("File not found or not a file: " + file.getAbsolutePath());
            }
        }
        return fileDetailsList;
    }
}
