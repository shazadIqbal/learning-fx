
package com.mls.learningfx.controller;

import com.mls.learningfx.model.Supplier;
import com.mls.learningfx.model.SupplierType;
import com.mls.learningfx.service.SupplierService;
import com.mls.learningfx.util.NavigationUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class SupplierController {

    @FXML
    private TableView<Supplier> supplierTable;

    @FXML
    private TableColumn<Supplier, Number> idColumn;

    @FXML
    private TableColumn<Supplier, String> nameColumn;

    @FXML
    private TableColumn<Supplier, String> contactColumn;

    @FXML
    private TableColumn<Supplier, String> typeColumn;

    @FXML
    private TableColumn<Supplier, String> companyNameColumn;

    @FXML
    private TableColumn<Supplier, Void> actionColumn;

    @FXML
    private Label statusLabel;

    private final SupplierService supplierService =
            new SupplierService();

    private final ObservableList<Supplier> supplierList =
            FXCollections.observableArrayList();

    private final ObservableList<SupplierType> supplierTypeList =
            FXCollections.observableArrayList();


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

        typeColumn.setCellValueFactory(
                new PropertyValueFactory<>("type")
        );

        companyNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("companyName")
        );

        supplierTable.setItems(supplierList);

        getAllSupplier();

        deleteSupplier();
    }


    @FXML
    private void addSupplier() {

        Dialog<Supplier> dialog = new Dialog<>();

        dialog.setTitle("Add Supplier");
        dialog.setHeaderText("Enter supplier details");

        ButtonType saveButton = new ButtonType(
                "Save Supplier",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                ButtonType.CANCEL
        );

        TextField nameField = new TextField();
        nameField.setPromptText("Supplier name");

        TextField contactField = new TextField();
        contactField.setPromptText("Contact");

        ComboBox<SupplierType> typeComboBox =
                new ComboBox<>();

        typeComboBox.setPromptText("Select supplier type");
        typeComboBox.setMaxWidth(Double.MAX_VALUE);


        loadSupplierTypes();

        typeComboBox.setItems(supplierTypeList);

        TextField companyNameField = new TextField();
        companyNameField.setPromptText("Company name");


        GridPane form = new GridPane();

        form.setHgap(12);
        form.setVgap(12);

        form.addRow(0, new Label("Supplier Name"), nameField);
        form.addRow(1, new Label("Contact"), contactField);
        form.addRow(2, new Label("Supplier Type"), typeComboBox);
        form.addRow(3, new Label("Company Name"), companyNameField);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(410);



        Node save = dialog.getDialogPane()
                .lookupButton(saveButton);

        save.addEventFilter(ActionEvent.ACTION, event -> {

            if (nameField.getText().isBlank()
                    || contactField.getText().isBlank()
                    || typeComboBox.getValue() == null
                    || companyNameField.getText().isBlank()) {

                event.consume();

                showError("Please fill all supplier details.");
            }
        });

        dialog.setResultConverter(button -> {

            if (button != saveButton) {
                return null;
            }

            return Supplier.builder()
                    .name(nameField.getText().trim())
                    .contact(contactField.getText().trim())
                    .typeId(typeComboBox.getValue().getId())
                    .companyName(companyNameField.getText().trim())
                    .build();
        });

        dialog.showAndWait().ifPresent(supplier -> {

            boolean added = supplierService.addSupplier(supplier);

            if (added) {

                getAllSupplier();

                statusLabel.setText("Supplier added successfully.");

                showSuccess("Supplier added successfully!");

            } else {

                statusLabel.setText("Supplier was not added.");

                showError("Supplier could not be inserted.");
            }
        });
    }

    private void loadSupplierTypes() {

        List<SupplierType> types =
                supplierService.getAllSupplierTypes();

        supplierTypeList.setAll(types);
    }


    @FXML
    public void deleteSupplier() {

        actionColumn.setCellFactory(col -> new TableCell<>() {

            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("X");

            {
                editButton.setOnAction(event -> {
                    Supplier supplier = getTableView().getItems().get(getIndex());
                    updateSupplier(supplier);
                });
                deleteButton.setOnAction(event -> {

                    Supplier supplier = getTableView()
                            .getItems()
                            .get(getIndex());

                    deleteSupplier(supplier);
                });
            }

            @Override
            protected void updateItem(Void unused, boolean empty) {

                super.updateItem(unused, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(10, editButton, deleteButton));
                }
            }
        });
    }

    private void updateSupplier(Supplier supplier) {
        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle("Update Supplier");
        dialog.setHeaderText("Update supplier details");

        ButtonType updateButton = new ButtonType("Update Supplier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButton, ButtonType.CANCEL);

        TextField nameField = new TextField(supplier.getName());
        nameField.setPromptText("Supplier name");
        TextField contactField = new TextField(supplier.getContact());
        contactField.setPromptText("Contact");
        ComboBox<SupplierType> typeComboBox = new ComboBox<>();
        typeComboBox.setPromptText("Select supplier type");
        typeComboBox.setMaxWidth(Double.MAX_VALUE);
        loadSupplierTypes();
        typeComboBox.setItems(FXCollections.observableArrayList(supplierTypeList));
        supplierTypeList.stream()
                .filter(type -> type.getId() == supplier.getTypeId())
                .findFirst().ifPresent(typeComboBox::setValue);
        TextField companyNameField = new TextField(supplier.getCompanyName());
        companyNameField.setPromptText("Company name");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.addRow(0, new Label("Supplier Name"), nameField);
        form.addRow(1, new Label("Contact"), contactField);
        form.addRow(2, new Label("Supplier Type"), typeComboBox);
        form.addRow(3, new Label("Company Name"), companyNameField);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(410);

        Node save = dialog.getDialogPane().lookupButton(updateButton);
        save.addEventFilter(ActionEvent.ACTION, event -> {
            if (nameField.getText().isBlank() || contactField.getText().isBlank()
                    || typeComboBox.getValue() == null || companyNameField.getText().isBlank()) {
                event.consume();
                showError("Please fill all supplier details.");
            }
        });

        dialog.setResultConverter(button -> {
            if (button != updateButton) return null;
            return Supplier.builder()
                    .id(supplier.getId())
                    .name(nameField.getText().trim())
                    .contact(contactField.getText().trim())
                    .typeId(typeComboBox.getValue().getId())
                    .companyName(companyNameField.getText().trim())
                    .build();
        });

        dialog.showAndWait().ifPresent(updated -> {
            if (supplierService.updateSupplier(updated)) {
                getAllSupplier();
                statusLabel.setText("Supplier updated successfully.");
                showSuccess("Supplier updated successfully!");
            } else {
                statusLabel.setText("Supplier was not updated.");
                showError("Supplier could not be updated.");
            }
        });
    }


    private void deleteSupplier(Supplier supplier) {

        boolean deleted =
                supplierService.deleteSupplier(supplier.getId());

        if (deleted) {

            supplierList.remove(supplier);

            showSuccess("Supplier successfully deleted!");

        } else {

            showError("Could not delete supplier.");
        }
    }


    @FXML
    public void getAllSupplier() {

        List<Supplier> suppliers = supplierService.getAllSuppliers();

        supplierList.setAll(suppliers);
    }


    // Success Alert
    private void showSuccess(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }


    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }


    @FXML
    private void goBack() {

        Stage stage =
                (Stage) supplierTable.getScene().getWindow();

        try {

            NavigationUtil.navigate(
                    stage,
                    NavigationUtil.DASHBOARD
            );

        } catch (IOException e) {

            e.printStackTrace();

            statusLabel.setText(
                    "Could not return to dashboard."
            );
        }
    }
}
