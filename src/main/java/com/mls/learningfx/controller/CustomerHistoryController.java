package com.mls.learningfx.controller;

import com.mls.learningfx.model.Customer;
import com.mls.learningfx.model.CustomerSaleHistory;
import com.mls.learningfx.service.SaleService;

import com.mls.learningfx.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import com.mls.learningfx.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;

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

    @FXML
    private Button backButton;

    private final SaleService saleService = new SaleService();

    private Integer currentCustomerId;

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

        TableColumn<CustomerSaleHistory, Void> actionColumn =
                new TableColumn<>("Action");

        actionColumn.setCellFactory(column -> new TableCell<>() {

            private final Button paidButton = new Button("Mark as Paid");

            {
                paidButton.setOnAction(event -> {
                    CustomerSaleHistory sale =
                            getTableView().getItems().get(getIndex());

                    try {
                        boolean updated = saleService.markCreditSaleAsSettled(
                                sale.getSaleId()
                        );

                        if (updated) {
                            loadHistory(currentCustomerId);
                        }

                    } catch (RuntimeException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Settlement Error");
                        alert.setHeaderText("Unable to settle credit sale");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    CustomerSaleHistory sale =
                            getTableView().getItems().get(getIndex());

                    boolean isCredit =
                            "Credit".equalsIgnoreCase(sale.getPaymentMode());

                    boolean isSettled =
                            Boolean.TRUE.equals(sale.getCreditSettled());

                    if (isCredit && !isSettled) {
                        setGraphic(paidButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        historyTable.getColumns().add(actionColumn);
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

        currentCustomerId = customer.getId();

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
    @FXML
    private void backToCustomers() {

        Stage stage = (Stage)
                backButton.getScene().getWindow();
        try {
            NavigationUtil.navigate(
                    stage,
                    NavigationUtil.CUSTOMER_VIEW
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
