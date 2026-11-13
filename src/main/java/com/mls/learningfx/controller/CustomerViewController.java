
package com.mls.learningfx.controller;

import com.mls.learningfx.model.Customer;
import com.mls.learningfx.service.CustomerService;

import com.mls.learningfx.util.NavigationUtil;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import com.mls.learningfx.HelloApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import com.mls.learningfx.controller.CustomerHistoryController;

public class CustomerViewController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Customer> tableView;

    @FXML
    private TableColumn<Customer, Integer> idColumn;

    @FXML
    private TableColumn<Customer, String> nameColumn;

    @FXML
    private TableColumn<Customer, String> contactColumn;

    @FXML
    private TableColumn<Customer, Void> actionColumn;

    @FXML
    private Button backButton;
    @FXML
    private Button historyBtn;


    private final CustomerService customerService =
            new CustomerService();

    // INITIALIZE
    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        contactColumn.setCellValueFactory(
                new PropertyValueFactory<>("contact")
        );

        addButton();

        getAllCustomer();

        // Search when pressing Enter
        searchField.setOnAction(e -> handleSearch());

        backButton.setOnAction(e -> backToDashboard());
    }

    private void backToDashboard() {
        Stage stage = (Stage)
                backButton.getScene().getWindow();

        try {

            NavigationUtil.navigate(
                    stage,
                    NavigationUtil.DASHBOARD
            );

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    // CREATE CUSTOMER
    @FXML
    public void addCustomer() {

        Dialog<Customer> dialog = new Dialog<>();

        dialog.setTitle("Add Customer");
        dialog.setHeaderText("Enter Customer Details");

        ButtonType saveButton = new ButtonType(
                "Save",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                ButtonType.CANCEL
        );

        TextField nameField = new TextField();
        nameField.setPromptText("Enter customer name");

        TextField contactField = new TextField();
        contactField.setPromptText("Enter contact number");

        GridPane grid = new GridPane();

        grid.setHgap(10);
        grid.setVgap(15);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Contact:"), 0, 1);
        grid.add(contactField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {

            if (button == saveButton) {
                return new Customer(
                        null,
                        nameField.getText().trim(),
                        contactField.getText().trim()
                );
            }

            return null;
        });

        Optional<Customer> result = dialog.showAndWait();

        result.ifPresent(customer -> {

            if (!validateCustomer(
                    customer.getName(),
                    customer.getContact())) {
                return;
            }

            boolean added = customerService.addCustomer(
                    customer.getName(),
                    customer.getContact()
            );

            if (added) {
                getAllCustomer();
                showSuccess("Customer Added Successfully!");
            } else {
                showError("Failed to Add Customer");
            }
        });
    }

    // READ ALL CUSTOMERS
    @FXML
    public void getAllCustomer() {

        ObservableList<Customer> result =
                customerService.getAllCustomer();

        tableView.setItems(result);
    }

    // SEARCH CUSTOMER
    @FXML
    public void handleSearch() {

        String keyword = searchField.getText().trim();

        ObservableList<Customer> result =
                customerService.searchCustomer(keyword);

        tableView.setItems(result);

        if (result.isEmpty()) {
            showError("No Customer Found!");
        }
    }

    // SHOW ALL CUSTOMERS
    @FXML
    public void handleShowAll() {

        searchField.clear();
        getAllCustomer();
    }

    // ACTION COLUMN
    @FXML
    private void addButton() {

        actionColumn.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button historyBtn = new Button("History");

            private final HBox hBox = new HBox(10);

            {

                editBtn.setOnAction(e -> {

                    Customer customer =
                            getTableRow().getItem();

                    if (customer != null) {
                        editCustomer(customer);
                    }
                });

                deleteBtn.setOnAction(e -> {

                    Customer customer =
                            getTableRow().getItem();

                    if (customer != null) {
                        deleteCustomer(customer);
                    }
                });
                historyBtn.setOnAction(e -> {

                    Customer customer =
                            getTableRow().getItem();

                    if (customer != null) {
                        viewCustomerHistory(customer);
                    }
                });

                hBox.getChildren().addAll(editBtn, deleteBtn, historyBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                setGraphic(empty ? null : hBox);
            }
        });
    }


    private void viewCustomerHistory(Customer customer) {


            Stage stage = (Stage) backButton.getScene().getWindow();
        try {
                NavigationUtil.navigate(stage, NavigationUtil.CUSTOMER_HISTORY_VIEW);

//            FXMLLoader loader = new FXMLLoader(
//                    HelloApplication.class.getResource(
//                            "customer-history-view.fxml"
//                    )
//            );
//
//            Scene scene = new Scene(loader.load());
//
//            // Get history controller
//            CustomerHistoryController controller =
//                    loader.getController();
//
//            // Pass selected customer
//            controller.setCustomer(customer);
//
//            // Create separate popup window
//            Stage historyStage = new Stage();
//
//            historyStage.setTitle(
//                    "Purchase History - " + customer.getName()
//            );
//
//            historyStage.initOwner(
//                    tableView.getScene().getWindow()
//            );
//
//            historyStage.initModality(Modality.APPLICATION_MODAL);
//
//            historyStage.setScene(scene);
//
//            historyStage.setMinWidth(900);
//            historyStage.setMinHeight(600);
//
//            historyStage.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException(e);

//            e.printStackTrace();
//
//            showError(
//                    "Unable to open customer purchase history."
//            );
        }
    }


    // EDIT CUSTOMER
    private void editCustomer(Customer customer) {

        Dialog<Customer> dialog = new Dialog<>();

        dialog.setTitle("Edit Customer");
        dialog.setHeaderText("Update Customer Details");

        ButtonType updateButton = new ButtonType(
                "Update",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                updateButton,
                ButtonType.CANCEL
        );

        TextField nameField = new TextField(customer.getName());
        TextField contactField = new TextField(customer.getContact());

        nameField.setPromptText("Customer name");
        contactField.setPromptText("Customer contact");

        GridPane grid = new GridPane();

        grid.setHgap(10);
        grid.setVgap(15);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Contact:"), 0, 1);
        grid.add(contactField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {

            if (button == updateButton) {

                return new Customer(
                        customer.getId(),
                        nameField.getText().trim(),
                        contactField.getText().trim()
                );
            }

            return null;
        });

        Optional<Customer> result = dialog.showAndWait();

        result.ifPresent(updatedCustomer -> {

            if (!validateCustomer(
                    updatedCustomer.getName(),
                    updatedCustomer.getContact())) {
                return;
            }

            boolean updated = customerService.updateCustomer(
                    updatedCustomer.getId(),
                    updatedCustomer.getName(),
                    updatedCustomer.getContact()
            );

            if (updated) {
                handleCurrentSearch();
                showSuccess("Customer Successfully Updated!");
            } else {
                showError("Failed to Update Customer");
            }
        });
    }

    // DELETE CUSTOMER
    private void deleteCustomer(Customer customer) {

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Delete Customer");
        confirmation.setHeaderText("Confirm Customer Deletion");

        confirmation.setContentText(
                "Are you sure you want to delete " +
                        customer.getName() + "?"
        );

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isPresent() &&
                result.get() == ButtonType.OK) {

            boolean deleted =
                    customerService.deleteCustomer(customer.getId());

            if (deleted) {
                handleCurrentSearch();
                showSuccess("Customer Successfully Deleted!");
            } else {
                showError("Failed to Delete Customer");
            }
        }
    }

    // REFRESH CURRENT SEARCH RESULTS
    private void handleCurrentSearch() {

        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            getAllCustomer();
        } else {
            ObservableList<Customer> result =
                    customerService.searchCustomer(keyword);

            tableView.setItems(result);
        }
    }

    // VALIDATION
    private boolean validateCustomer(String name, String contact) {

        if (name == null || name.trim().isEmpty()) {
            showError("Customer Name Cannot Be Empty!");
            return false;
        }

        if (contact == null || contact.trim().isEmpty()) {
            showError("Customer Contact Cannot Be Empty!");
            return false;
        }

        return true;
    }

    // SUCCESS ALERT
    private void showSuccess(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.show();
    }

    // ERROR ALERT
    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Failed");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.show();
    }
}
