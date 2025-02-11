package com.endpointexplorers.server.component;

import com.endpointexplorers.server.utils.DirectoryUtils;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Component
public class PlotFactory {

    @Value("${path.serverPlotsResources}")
    private String serverResourcesPath;

    private Path sourcePath;

    @PostConstruct
    public void init() {
        this.sourcePath = Paths.get(serverResourcesPath).toAbsolutePath();
    }

    public String createPlot(
            String folderName,
            String metricName,
            String algorithmName,
            String problemName,
            List<Integer> iterations,
            List<Double> values) {

        checkInputData(iterations, values);
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(800)
                .title("Metrics: " + metricName)
                .xAxisTitle("Iterations")
                .yAxisTitle("Values")
                .build();
        chart.addSeries(metricName, iterations, values);

        return savePlot(chart, folderName, metricName, algorithmName, problemName);
    }

    private static void checkInputData(List<Integer> iterations, List<Double> values) {
        if (iterations == null || values == null || iterations.size() != values.size()) {
            throw new IllegalArgumentException("Iterations and Values cannot be empty or have a different size.");
        }
    }

    private String savePlot(XYChart chart, String folderName, String metricName, String algorithmName, String problemName) {
        String finalPath = createFinalPath(folderName, metricName, algorithmName, problemName);

        try {
            BitmapEncoder.saveBitmap(chart, finalPath, BitmapEncoder.BitmapFormat.PNG);
            System.out.println("Plot saved successfully.");
        } catch (Exception e) {
            System.out.println("Error while saving the plot.");
            throw new RuntimeException("An error occurred while saving the plot.", e);
        }
        return finalPath;
    }

    private String createFinalPath(String folderName, String metricName, String algorithmName, String problemName) {
        String metricsDirPath = sourcePath.resolve(folderName).toString();
        DirectoryUtils.ensureDirectoryExists(metricsDirPath);

        return metricsDirPath + "/" + metricName + "-" + algorithmName + "-" + problemName + ".png";
    }
}
