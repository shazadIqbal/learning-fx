
package com.mls.learningfx.controller;

import com.mls.learningfx.service.ReportService;
import com.mls.learningfx.util.NavigationUtil;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

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
}
