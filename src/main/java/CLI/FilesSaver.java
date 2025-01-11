package CLI;

import CLI.component.FileDetails;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class FilesSaver {
    private static final String CLIENT_BASE_PATH = "src/main/java/CLI/clientResources/plots/";

    public static void saveFiles(List<FileDetails> fileDetailsList) {
        if (!fileDetailsList.isEmpty()) {
            for (FileDetails file : fileDetailsList) {
                saveFile(file);
            }
        }
    }

    private static void saveFile(FileDetails file) {
        try {
            byte[] fileContent = Base64.getDecoder().decode(file.getContentBase64());
            Path filePath = Paths.get(CLIENT_BASE_PATH + file.getFileName());
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, fileContent);
            System.out.println("File saved to: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to save file: " + file.getFileName());
            e.printStackTrace();
        }
    }
}
