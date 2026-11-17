package com.mls.learningfx.controller;

import com.mls.learningfx.model.Product;
import com.mls.learningfx.service.ProductService;
import com.mls.learningfx.util.NavigationUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ExpiryReportController {

    @FXML
    private Button backButton;

    @FXML
    private Label expiredCountLabel;

    @FXML
    private Label nearExpiryCountLabel;

    @FXML
    private Label totalTrackedCountLabel;

    @FXML
    private TableView<Product> expiryTable;

    @FXML
    private TableColumn<Product, Integer> idColumn;

    @FXML
    private TableColumn<Product, String> nameColumn;

    @FXML
    private TableColumn<Product, String> barcodeColumn;

    @FXML
    private TableColumn<Product, String> categoryColumn;

    @FXML
    private TableColumn<Product, Integer> stockColumn;

    @FXML
    private TableColumn<Product, Double> sellPriceColumn;

    @FXML
    private TableColumn<Product, LocalDate> expiryDateColumn;

    @FXML
    private TableColumn<Product, Product> statusColumn;

    private final ProductService productService = new ProductService();

    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        barcodeColumn.setCellValueFactory(
                new PropertyValueFactory<>("barcode")
        );

        categoryColumn.setCellValueFactory(
                new PropertyValueFactory<>("categoryName")
        );

        stockColumn.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        sellPriceColumn.setCellValueFactory(
                new PropertyValueFactory<>("sellPrice")
        );

        expiryDateColumn.setCellValueFactory(
                new PropertyValueFactory<>("expiryDate")
        );

        configureStatusColumn();

        expiryTable.setPlaceholder(
                new Label("No products with expiry date found.")
        );

        loadSummaryAndTable();
    }

    private void configureStatusColumn() {

        statusColumn.setCellValueFactory(
                cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue())
        );

        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);

                if (empty || product == null || product.getExpiryDate() == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                LocalDate today = LocalDate.now();
                LocalDate exp = product.getExpiryDate();

                if (exp.isBefore(today)) {
                    long daysAgo = ChronoUnit.DAYS.between(exp, today);
                    setText("EXPIRED (" + daysAgo + "d ago)");
                    setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                } else if (!exp.isAfter(today.plusDays(30))) {
                    long daysLeft = ChronoUnit.DAYS.between(today, exp);
                    setText("Expiring Soon (" + daysLeft + "d left)");
                    setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                } else {
                    long daysLeft = ChronoUnit.DAYS.between(today, exp);
                    setText("Valid (" + daysLeft + "d left)");
                    setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void loadSummaryAndTable() {
        ObservableList<Product> allWithExpiry = productService.getProductsWithExpiry();
        ObservableList<Product> expired = productService.getExpiredProducts();
        ObservableList<Product> nearExpiry = productService.getNearExpiryProducts(30);

        expiredCountLabel.setText(String.valueOf(expired.size()));
        nearExpiryCountLabel.setText(String.valueOf(nearExpiry.size()));
        totalTrackedCountLabel.setText(String.valueOf(allWithExpiry.size()));

        expiryTable.setItems(allWithExpiry);
    }

    @FXML
    private void showAllExpiryProducts(ActionEvent event) {
        expiryTable.setItems(productService.getProductsWithExpiry());
    }

    @FXML
    private void showExpiredOnly(ActionEvent event) {
        expiryTable.setItems(productService.getExpiredProducts());
    }

    @FXML
    private void showNearExpiryOnly(ActionEvent event) {
        expiryTable.setItems(productService.getNearExpiryProducts(30));
    }

    @FXML
    private void handleBack(ActionEvent event) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        try {
            NavigationUtil.navigate(stage, NavigationUtil.DASHBOARD);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
