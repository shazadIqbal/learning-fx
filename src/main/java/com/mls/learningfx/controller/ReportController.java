
package com.mls.learningfx.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

public class ReportController {

    @FXML
    private BarChart<String, Number> reportChart;

    @FXML
    private PieChart pieChart;

    @FXML
    public void initialize() {


        XYChart.Series<String, Number> series = new XYChart.Series<>();

        series.setName("Product Stock");

        series.getData().add(new XYChart.Data<>("Laptop", 50));
        series.getData().add(new XYChart.Data<>("Mouse", 80));
        series.getData().add(new XYChart.Data<>("Keyboard", 40));
        series.getData().add(new XYChart.Data<>("Monitor", 65));
        series.getData().add(new XYChart.Data<>("Headphones", 90));

        reportChart.getData().add(series);



        pieChart.getData().add(new PieChart.Data("Laptop", 50));
        pieChart.getData().add(new PieChart.Data("Mouse", 80));
        pieChart.getData().add(new PieChart.Data("Keyboard", 40));
        pieChart.getData().add(new PieChart.Data("Monitor", 65));
        pieChart.getData().add(new PieChart.Data("Headphones", 90));

        pieChart.setTitle("Product Sales");
    }
}
