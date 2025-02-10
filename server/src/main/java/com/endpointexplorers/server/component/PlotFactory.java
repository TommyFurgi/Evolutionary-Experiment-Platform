package com.endpointexplorers.server.component;

import com.endpointexplorers.server.utils.DirectoryUtils;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class PlotFactory {
    private static final String BASE_PATH = "src/main/java/com/example/Endpoint_Explorers/serverResources/plots/";

    public static String createPlot(
            String metricName,
            String algorithmName,
            String problemName,
            Timestamp startDate,
            Timestamp endDate,
            List<Integer> iterations,
            List<Double> values) {

        checkInputData(iterations, values);
        DirectoryUtils.ensureDirectoryExists(BASE_PATH);
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(800)
                .title("Metrics: " + metricName)
                .xAxisTitle("Iterations")
                .yAxisTitle("Values")
                .build();
        chart.addSeries(metricName, iterations, values);

        return savePlot(chart, metricName, algorithmName, problemName, startDate, endDate);
    }


    private static void checkInputData(List<Integer> iterations, List<Double> values) {
        if (iterations == null || values == null || iterations.size() != values.size()) {
            throw new IllegalArgumentException("Iterations and Values cannot be empty or have a different size.");
        }
    }

    private static String savePlot(XYChart chart, String metricName, String algorithmName, String problemName, Timestamp startDate, Timestamp endDate) {
        String finalPath = createFinalPath(metricName, algorithmName, problemName, startDate, endDate);

        try {
            BitmapEncoder.saveBitmap(chart, finalPath, BitmapEncoder.BitmapFormat.PNG);
            System.out.println("Plot saved successfully.");
        } catch (Exception e) {
            System.out.println("Error while saving the plot.");
        }
        return finalPath;
    }

    private static String createFinalPath(String metricName, String algorithmName, String problemName, Timestamp startDate, Timestamp endDate) {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String formattedStartDate = dataFormat.format(startDate);
        String formattedEndDate = dataFormat.format(endDate);

        return createMetricNamePath(metricName)
                + algorithmName + "_" + problemName + "_"
                + formattedStartDate + "_" + formattedEndDate + ".png";
    }

    private static String createMetricNamePath(String metricName) {
        String metricsDirPath = BASE_PATH + metricName + "/";
        File metricsDir = new File(metricsDirPath);
        if (!metricsDir.exists() && !metricsDir.mkdirs()) {
            System.out.println("There is a problem with directory creation: " + metricsDirPath);
        }
        return metricsDirPath;
    }
}
