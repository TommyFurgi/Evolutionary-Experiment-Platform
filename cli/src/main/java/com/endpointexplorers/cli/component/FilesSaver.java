package com.endpointexplorers.cli.component;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class FilesSaver {
    private final String cliPlotsResourcesPath;
    private final String cliCSVsResourcesPath;
    private final String cliOthersResourcesPath;

    @Inject
    public FilesSaver(
            @Named("cliPlotsResources") String cliPlotsResourcesPath,
            @Named("cliCSVsResources") String cliCSVsResourcesPath,
            @Named("cliOthersResources") String cliOthersResourcesPath
    ) {
        this.cliPlotsResourcesPath = cliPlotsResourcesPath;
        this.cliCSVsResourcesPath = cliCSVsResourcesPath;
        this.cliOthersResourcesPath = cliOthersResourcesPath;
    }

    public void saveFiles(List<FileDetails> fileDetailsList) {
        if (!fileDetailsList.isEmpty()) {
            for (FileDetails file : fileDetailsList) {
                saveFile(file);
            }
        }
    }

    private void saveFile(FileDetails file) {
        try {
            byte[] fileContent = Base64.getDecoder().decode(file.getContentBase64());
            String fileName = file.getFileName();
            Path filePath = getFilePath(fileName);

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, fileContent);
            System.out.println("File saved to: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to save file: " + file.getFileName());
        }
    }

    private Path getFilePath(String fileName) {
        if (fileName.endsWith(".png")) {
            return Paths.get(cliPlotsResourcesPath, fileName);
        } else if (fileName.endsWith(".csv")) {
            return Paths.get(cliCSVsResourcesPath, fileName);
        } else {
            return Paths.get(cliOthersResourcesPath, fileName);
        }
    }
}
