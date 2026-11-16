package com.mls.learningfx.controller;

import com.mls.learningfx.model.Customer;
import com.mls.learningfx.model.CustomerSaleHistory;
import com.mls.learningfx.model.Item;
import com.mls.learningfx.model.SaleRecord;
import com.mls.learningfx.service.SaleService;
import com.mls.learningfx.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class SaleRecordController {

    @FXML
    private Button backButton;

    @FXML
    private Button viewAllSalesButton;

    @FXML
    private TextField saleIdField;

    @FXML
    private Label customerNameLabel;

    @FXML
    private Label customerIdLabel;

    @FXML
    private Label customerContactLabel;

    @FXML
    private Label customerTotalPurchasesLabel;

    @FXML
    private Label customerLifetimeSpendingLabel;

    @FXML
    private Label saleIdLabel;

    @FXML
    private Label saleDateLabel;

    @FXML
    private Label paymentModeLabel;

    @FXML
    private Label soldByLabel;

    @FXML
    private Label totalItemsQtyLabel;

    @FXML
    private Label grandTotalLabel;

    @FXML
    private TableView<Item> itemsTable;

    @FXML
    private TableColumn<Item, Integer> productIdColumn;

    @FXML
    private TableColumn<Item, String> productNameColumn;

    @FXML
    private TableColumn<Item, Integer> quantityColumn;

    @FXML
    private TableColumn<Item, Double> priceColumn;

    @FXML
    private TableColumn<Item, Double> subTotalColumn;

    private final SaleService saleService = new SaleService();

    private Customer currentCustomer;
    private List<CustomerSaleHistory> currentCustomerHistory = Collections.emptyList();

    @FXML
    public void initialize() {

        productIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("productId")
        );

        productNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        quantityColumn.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        priceColumn.setCellValueFactory(
                new PropertyValueFactory<>("price")
        );

        subTotalColumn.setCellValueFactory(
                new PropertyValueFactory<>("subTotal")
        );

        itemsTable.setPlaceholder(
                new Label("Enter a Sale ID above and click Search to view sale details.")
        );

        saleIdField.setOnAction(this::handleSearchSale);
    }

    @FXML
    private void handleSearchSale(ActionEvent event) {

        String input = saleIdField.getText() == null ? "" : saleIdField.getText().trim();

        if (input.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a Sale ID.");
            return;
        }

        int saleId;
        try {
            saleId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Sale ID must be a valid number.");
            return;
        }

        loadSaleById(saleId);
    }

    private void loadSaleById(int saleId) {
        try {
            SaleRecord record = saleService.getSaleRecordById(saleId);

            if (record == null) {
                clearDisplay();
                showAlert(Alert.AlertType.INFORMATION, "Not Found",
                        "No sale record found for Sale ID: " + saleId);
                return;
            }

            saleIdField.setText(String.valueOf(saleId));
            populateSaleRecord(record);

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void populateSaleRecord(SaleRecord record) {

        Customer customer = record.getCustomer();

        if (customer != null && customer.getId() != null) {
            currentCustomer = customer;
            customerNameLabel.setText(customer.getName());
            customerIdLabel.setText("Customer ID: " + customer.getId());
            customerContactLabel.setText("Contact: " + customer.getContact());

            // Reuse existing SaleService methods for customer history & total spendings
            currentCustomerHistory =
                    saleService.getCustomerSaleHistory(customer.getId());
            BigDecimal lifetimeSpending =
                    saleService.getCustomerTotalSpendings(customer.getId());

            customerTotalPurchasesLabel.setText(String.valueOf(currentCustomerHistory.size()));
            customerLifetimeSpendingLabel.setText(
                    "Rs. " + String.format("%,.2f", lifetimeSpending)
            );
            viewAllSalesButton.setDisable(false);
        } else {
            currentCustomer = null;
            currentCustomerHistory = Collections.emptyList();
            customerNameLabel.setText("Walk-in Customer");
            customerIdLabel.setText("Customer ID: N/A");
            customerContactLabel.setText("Contact: N/A");
            customerTotalPurchasesLabel.setText("-");
            customerLifetimeSpendingLabel.setText("Rs. 0.00");
            viewAllSalesButton.setDisable(true);
        }

        saleIdLabel.setText("#" + record.getSaleId());
        saleDateLabel.setText(record.getSaleDate());
        paymentModeLabel.setText(record.getPaymentMode());
        soldByLabel.setText(record.getSoldBy());

        List<Item> items = record.getItems() != null
                ? record.getItems()
                : Collections.emptyList();

        int totalQty = items.stream()
                .mapToInt(Item::getQuantity)
                .sum();

        totalItemsQtyLabel.setText(items.size() + " Products (" + totalQty + " Qty)");

        BigDecimal total = record.getTotalPrice() != null
                ? record.getTotalPrice()
                : BigDecimal.ZERO;

        grandTotalLabel.setText("Rs. " + String.format("%,.2f", total));

        itemsTable.setItems(FXCollections.observableArrayList(items));
    }

    @FXML
    private void handleViewAllCustomerSales(ActionEvent event) {
        if (currentCustomer == null || currentCustomer.getId() == null) {
            return;
        }

        Stage popupStage = new Stage();
        popupStage.setTitle("All Sales - " + currentCustomer.getName());
        popupStage.initOwner(viewAllSalesButton.getScene().getWindow());
        popupStage.initModality(Modality.APPLICATION_MODAL);

        Label titleLabel = new Label("All Sales of " + currentCustomer.getName() + " (" + currentCustomer.getContact() + ")");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label hintLabel = new Label("Double-click any row to load that Sale ID:");
        hintLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        TableView<CustomerSaleHistory> salesTable = new TableView<>();

        // Left side: Date
        TableColumn<CustomerSaleHistory, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        dateCol.setPrefWidth(260);

        // Right side: Sale ID
        TableColumn<CustomerSaleHistory, Integer> saleIdCol = new TableColumn<>("Sale ID");
        saleIdCol.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        saleIdCol.setPrefWidth(160);

        salesTable.getColumns().addAll(dateCol, saleIdCol);
        salesTable.setItems(FXCollections.observableArrayList(currentCustomerHistory));

        salesTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                CustomerSaleHistory selected = salesTable.getSelectionModel().getSelectedItem();
                if (selected != null && selected.getSaleId() != null) {
                    popupStage.close();
                    loadSaleById(selected.getSaleId());
                }
            }
        });

        VBox layout = new VBox(12, titleLabel, hintLabel, salesTable);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f4f6f9;");

        Scene scene = new Scene(layout, 480, 420);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    private void clearDisplay() {
        currentCustomer = null;
        currentCustomerHistory = Collections.emptyList();
        viewAllSalesButton.setDisable(true);

        customerNameLabel.setText("-");
        customerIdLabel.setText("Customer ID: -");
        customerContactLabel.setText("Contact: -");
        customerTotalPurchasesLabel.setText("0");
        customerLifetimeSpendingLabel.setText("Rs. 0.00");

        saleIdLabel.setText("-");
        saleDateLabel.setText("-");
        paymentModeLabel.setText("-");
        soldByLabel.setText("-");
        totalItemsQtyLabel.setText("0");
        grandTotalLabel.setText("Rs. 0.00");

        itemsTable.getItems().clear();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        try {
            NavigationUtil.navigate(stage, NavigationUtil.SALE_VIEW);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

