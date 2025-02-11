package com.endpointexplorers.cli.component;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class MetricsAndFiles {
    private final Map<String, List<Double>> metrics;
    private final List<FileDetails> files;

    @JsonCreator
    public MetricsAndFiles(
            @JsonProperty("metrics") Map<String, List<Double>> metrics,
            @JsonProperty("files") List<FileDetails> files) {
        this.metrics = metrics;
        this.files = files;
    }
}
