package com.endpointexplorers.server.component;

import com.endpointexplorers.server.model.FileDetails;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class FileContentConverter {
    @Value("${path.serverPlotsResources}")
    private String serverResourcesPath;
    private Path sourcePath;

    @PostConstruct
    public void init() {
        this.sourcePath = Paths.get(serverResourcesPath).toAbsolutePath();
    }

    public List<FileDetails> createFilesDetails(List<String> fileNamePathsList) {
        List<FileDetails> fileDetailsList = new ArrayList<>();
        for (String fullFilePath : fileNamePathsList) {
            Path fullPath = Paths.get(fullFilePath);
            File file = new File(fullFilePath);
            if (file.exists() && file.isFile()) {
                try {
                    Path relativePath = sourcePath.relativize(fullPath);
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
