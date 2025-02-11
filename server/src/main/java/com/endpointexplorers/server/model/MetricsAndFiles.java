package com.endpointexplorers.server.model;

import java.util.List;
import java.util.Map;

public record MetricsAndFiles(Map<String, List<Double>> metrics, List<FileDetails> files) {
}
