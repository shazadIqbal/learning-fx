package com.mls.learningfx.controller;

import com.mls.learningfx.model.Customer;
import com.mls.learningfx.model.CustomerSaleHistory;
import com.mls.learningfx.service.SaleService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.List;

public class CustomerHistoryController {

    @FXML
    private Label customerNameLabel;

    @FXML
    private Label customerIdLabel;

    @FXML
    private Label customerContactLabel;

    @FXML
    private Label totalPurchasesLabel;

    @FXML
    private Label totalSpendingsLabel;

    @FXML
    private TableView<CustomerSaleHistory> historyTable;

    @FXML
    private TableColumn<CustomerSaleHistory, Integer> saleIdColumn;

    @FXML
    private TableColumn<CustomerSaleHistory, String> dateColumn;

    @FXML
    private TableColumn<CustomerSaleHistory, String> paymentColumn;

    @FXML
    private TableColumn<CustomerSaleHistory, String> itemsColumn;

    @FXML
    private TableColumn<CustomerSaleHistory, BigDecimal> totalColumn;

    private final SaleService saleService = new SaleService();

    @FXML
    public void initialize() {

        saleIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("saleId")
        );

        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("purchaseDate")
        );

        paymentColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentMode")
        );

        itemsColumn.setCellValueFactory(
                new PropertyValueFactory<>("purchasedItems")
        );

        totalColumn.setCellValueFactory(
                new PropertyValueFactory<>("totalPrice")
        );
    }

    // LOAD SELECTED CUSTOMER HISTORY
    public void setCustomer(Customer customer) {

        customerNameLabel.setText(customer.getName());

        customerIdLabel.setText(
                "Customer ID: " + customer.getId()
        );

        customerContactLabel.setText(
                "Contact: " + customer.getContact()
        );

        loadHistory(customer.getId());
    }

    private void loadHistory(Integer customerId) {

        List<CustomerSaleHistory> history =
                saleService.getCustomerSaleHistory(customerId);

        historyTable.setItems(
                FXCollections.observableArrayList(history)
        );

        // TOTAL PURCHASES
        totalPurchasesLabel.setText(
                String.valueOf(history.size())
        );

        // TOTAL SPENDINGS
        BigDecimal total =
                saleService.getCustomerTotalSpendings(customerId);

        totalSpendingsLabel.setText(
                "Rs. " + String.format("%,.2f", total)
        );

        if (history.isEmpty()) {
            historyTable.setPlaceholder(
                    new Label("No purchase history available.")
            );
        }
    }
}
