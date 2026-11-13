
package com.mls.learningfx.controller;

import com.mls.learningfx.service.MonthlyReportService;
import com.mls.learningfx.service.ReportService;
import com.mls.learningfx.util.NavigationUtil;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

public class ReportController {

    @FXML
    private BarChart<String, Number> reportChart;

    @FXML
    private BarChart<String, Number> salesChart;

    @FXML
    private BarChart<String, Number> profitChart;


    @FXML
    private BarChart<String, Number> salesDistributionChart;

    @FXML
    private Button backBtn;

    private ReportService reportService;


    @FXML
    public void initialize() {

        refreshReports();
    }


    @FXML
    private void refreshReports() {

        try {
            reportService = new ReportService();

            loadLowStockReport();
            loadSalesReport();
            loadProfitReport();
            loadMostSellingReport();

        } catch (Exception e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Report Error");
            alert.setHeaderText("Unable to load reports");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }


    private void loadLowStockReport() {

        Map<String, Integer> data = reportService.getLowStockProducts();

        reportChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        series.setName("Available Stock");

        for (Map.Entry<String, Integer> entry : data.entrySet()) {

            series.getData().add( new XYChart.Data<>(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        reportChart.getData().add(series);
        showBarValues(series);
    }
    private void showBarValues(XYChart.Series<String, Number> series) {

        for (XYChart.Data<String, Number> data : series.getData()) {

            StackPane bar = (StackPane) data.getNode();

            Label label = new Label(data.getYValue().toString());

            label.setStyle(
                    "-fx-font-weight: bold;" +
                            "-fx-text-fill: white;"
            );

            bar.getChildren().add(label);
        }
    }


    private void loadSalesReport() {

        Map<String, Double> data = reportService.getDayWiseSales();

        salesChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        series.setName("Total Sales");

        for (Map.Entry<String, Double> entry : data.entrySet()) {

            series.getData().add( new XYChart.Data<>(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        salesChart.getData().add(series);
        showBarValues(series);
    }


    private void loadProfitReport() {

        Map<String, Double> data = reportService.getDayWiseProfit();

        profitChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        series.setName("Total Profit");

        for (Map.Entry<String, Double> entry : data.entrySet()) {

            series.getData().add( new XYChart.Data<>(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        profitChart.getData().add(series);
        showBarValues(series);
    }


    private void loadMostSellingReport() {

        Map<String, Integer> data = reportService.getMostSellingProducts();

        salesDistributionChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        series.setName("Quantity Sold");

        for (Map.Entry<String, Integer> entry : data.entrySet()) {

            series.getData().add(
                    new XYChart.Data<>(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        salesDistributionChart.getData().add(series);
        showBarValues(series);
    }


    @FXML
    private void goBack() {

        Stage stage = (Stage) backBtn.getScene().getWindow();

        try {

            NavigationUtil.navigate(
                    stage,
                    NavigationUtil.DASHBOARD
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
    private final MonthlyReportService monthlyReportService = new MonthlyReportService();

    // ---------- 1. Button dabane par yeh chalta hai ----------
    @FXML
    private void downloadMonthlyReport() {

        // Pehle dialog dikhao, aur user se mahine pucho
        ComboBox<YearMonth> fromBox = createMonthBox();
        ComboBox<YearMonth> toBox = createMonthBox();

        boolean okPressed = showMonthDialog(fromBox, toBox);
        if (!okPressed) {
            return;   // user ne Cancel dabaya
        }

        YearMonth from = fromBox.getValue();
        YearMonth to = toBox.getValue();

        // Phir report banao
        generateReport(from, to);
    }

    // ---------- 2. Mahine ki dropdown banao ----------
    private ComboBox<YearMonth> createMonthBox() {

        ComboBox<YearMonth> box = new ComboBox<>();
        YearMonth now = YearMonth.now();

        // Pichle 24 mahine, naya mahina sab se upar
        for (int i = 0; i < 24; i++) {
            box.getItems().add(now.minusMonths(i));
        }

        // "2026-11" ki jagah "Nov 2026" dikhao
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM yyyy");
        box.setConverter(new StringConverter<>() {
            @Override
            public String toString(YearMonth month) {
                if (month == null) {
                    return "";
                }
                return month.format(format);
            }

            @Override
            public YearMonth fromString(String text) {
                return null;
            }
        });

        // Shuru mein is mahine ko select rakho
        box.setValue(now);
        return box;
    }

    // ---------- 3. Dialog dikhao, OK dabane par true wapas do ----------
    private boolean showMonthDialog(ComboBox<YearMonth> fromBox, ComboBox<YearMonth> toBox) {

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.addRow(0, new Label("Start month"), fromBox);
        grid.addRow(1, new Label("End month"), toBox);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Monthly Report");
        dialog.setHeaderText("Kis mahine se kis mahine tak report chahiye?");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> choice = dialog.showAndWait();
        return choice.isPresent() && choice.get() == ButtonType.OK;
    }

    // ---------- 4. Background mein report banao ----------
    private void generateReport(YearMonth from, YearMonth to) {

        // Task = background kaam, taake app freeze na ho
        Task<Path> task = new Task<>() {
            @Override
            protected Path call() throws Exception {
                return monthlyReportService.generateMonthlyReport(from, to);
            }
        };

        task.setOnSucceeded(e -> openPdf(task.getValue()));
        task.setOnFailed(e -> showError(task.getException()));

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // ---------- 5. PDF khol do ----------
    private void openPdf(Path file) {
        try {
            Desktop.getDesktop().open(file.toFile());
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Monthly Report");
            alert.setHeaderText("Report save ho gayi");
            alert.setContentText(file.toString());
            alert.show();
        }
    }

    // ---------- 6. Error dikhao ----------
    private void showError(Throwable error) {
        error.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Monthly Report");
        alert.setHeaderText("Report nahi bani");
        alert.setContentText(error.getMessage());
        alert.show();
    }

}
