package com.endpointexplorers.cli.component;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class FileDetails {
    private final String fileName;
    private final String contentBase64;

    @JsonCreator
    public FileDetails(@JsonProperty("filename") String fileName, @JsonProperty("contentBase64") String contentBase64) {
        this.fileName = fileName;
        this.contentBase64 = contentBase64;
    }
}
