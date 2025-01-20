package com.example.Endpoint_Explorers.utils;

import java.io.File;

public class DirectoryUtils {
    public static void ensureDirectoryExists(String path) {
        File dir = new File(path);
        if (!dir.exists() && !dir.mkdirs()) {
            System.out.println("There is a problem with directory creation: " + path);
        }
    }
}
