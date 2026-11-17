
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
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
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
    private BarChart<String, Number> mostSellingChart;

    @FXML
    private PieChart pieChart;

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
    }


    private void loadMostSellingReport() {

        Map<String, Integer> data = reportService.getMostSellingProducts();

        mostSellingChart.getData().clear();
        pieChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        series.setName("Quantity Sold");

        for (Map.Entry<String, Integer> entry : data.entrySet()) {

            series.getData().add(
                    new XYChart.Data<>(
                            entry.getKey(),
                            entry.getValue()
                    )
            );

            pieChart.getData().add(
                    new PieChart.Data(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        mostSellingChart.getData().add(series);
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
    // ---------- 1. Button dabane par yeh chalta hai ----------
    @FXML
    private void downloadMonthlyReport() {

        // Date picker wala dialog dikhao
        Optional<LocalDate[]> result = showReportDialog();

        if (result.isEmpty()) {
            return;   // user ne Cancel dabaya
        }

        LocalDate[] dates = result.get();

        // Date se month nikalo
        YearMonth from = YearMonth.from(dates[0]);
        YearMonth to = YearMonth.from(dates[1]);

        // Phir report banao
        generateReport(from, to);
    }

    // ---------- 2. Date picker wala dialog ----------
    private Optional<LocalDate[]> showReportDialog() {

        Dialog<LocalDate[]> dialog = new Dialog<>();
        dialog.setTitle("Monthly Report");
        dialog.setHeaderText("(From which date to which date?)");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        DatePicker fromPicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker toPicker = new DatePicker(LocalDate.now());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.add(new Label("Start date"), 0, 0);
        grid.add(fromPicker, 1, 0);
        grid.add(new Label("End date"), 0, 1);
        grid.add(toPicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Galat input ho to dialog band na ho
        dialog.getDialogPane().lookupButton(okButton).addEventFilter(
                javafx.event.ActionEvent.ACTION, event -> {
                    LocalDate start = fromPicker.getValue();
                    LocalDate end = toPicker.getValue();

                    if (start == null || end == null) {
                        new Alert(Alert.AlertType.ERROR, "Dono date chunein!").show();
                        event.consume();
                    } else if (start.isAfter(end)) {
                        new Alert(Alert.AlertType.ERROR,
                                "Start date, End date se pehle honi chahiye!").show();
                        event.consume();
                    }
                });

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                return new LocalDate[]{fromPicker.getValue(), toPicker.getValue()};
            }
            return null;
        });

        return dialog.showAndWait();
    }
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
