package com.mls.learningfx.controller;

import com.mls.learningfx.model.Item;
import com.mls.learningfx.service.SaleService;
import com.mls.learningfx.service.UserService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UpdateSaleController {

    @FXML
    private Label invoiceLabel;

    @FXML
    private TableView<Item> itemsTable;

    @FXML
    private TableColumn<Item, String> productColumn;

    @FXML
    private TableColumn<Item, Integer> boughtColumn;

    @FXML
    private TableColumn<Item, Integer> qtyColumn;

    @FXML
    private TableColumn<Item, Double> priceColumn;

    @FXML
    private TableColumn<Item, Double> subTotalColumn;

    @FXML
    private TableColumn<Item, Void> actionColumn;

    @FXML
    private Label refundLabel;

    private final SaleService saleService = new SaleService();

    private int saleId;

    // items as they were sold
    private List<Item> soldItems = new ArrayList<>();

    // items the customer keeps, changed by - + Delete
    private final ObservableList<Item> keptItems = FXCollections.observableArrayList();

    private boolean saved = false;

    @FXML
    public void initialize() {

        productColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        boughtColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(getBoughtQty(cell.getValue()))
        );

        qtyColumn.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        priceColumn.setCellValueFactory(
                new PropertyValueFactory<>("price")
        );

        subTotalColumn.setCellValueFactory(
                new PropertyValueFactory<>("subTotal")
        );

        addActionButtons();

        itemsTable.setItems(keptItems);
    }

    // called by SaleViewController before showing the popup
    public void setSale(int saleId, List<Item> soldItems) {

        this.saleId = saleId;
        this.soldItems = soldItems;

        invoiceLabel.setText("Invoice #" + saleId);

        // Copy the items so we can change qty
        for (Item item : soldItems) {
            keptItems.add(
                    Item.builder()
                            .productId(item.getProductId())
                            .name(item.getName())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .subTotal(item.getSubTotal())
                            .build()
            );
        }

        updateRefundLabel();
    }

    // true if the return was saved
    public boolean isSaved() {
        return saved;
    }

    private void addActionButtons() {

        actionColumn.setCellFactory(column -> new TableCell<Item, Void>() {

            private final Button decreaseBtn = new Button("-");
            private final Button increaseBtn = new Button("+");
            private final Button deleteBtn = new Button("Delete");

            private final HBox buttons = new HBox(
                    5, decreaseBtn, increaseBtn, deleteBtn
            );

            {
                decreaseBtn.setOnAction(event -> {
                    Item item = getTableView().getItems().get(getIndex());
                    decreaseQuantity(item);
                });

                increaseBtn.setOnAction(event -> {
                    Item item = getTableView().getItems().get(getIndex());
                    increaseQuantity(item);
                });

                deleteBtn.setOnAction(event -> {
                    Item item = getTableView().getItems().get(getIndex());
                    keptItems.remove(item);
                    updateRefundLabel();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void decreaseQuantity(Item item) {

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            item.setSubTotal(item.getQuantity() * item.getPrice());
        } else {
            keptItems.remove(item);
        }

        itemsTable.refresh();
        updateRefundLabel();
    }

    private void increaseQuantity(Item item) {

        int boughtQty = getBoughtQty(item);

        if (item.getQuantity() >= boughtQty) {
            showAlert(Alert.AlertType.WARNING, "Limit",
                    "Customer only bought " + boughtQty + ".");
            return;
        }

        item.setQuantity(item.getQuantity() + 1);
        item.setSubTotal(item.getQuantity() * item.getPrice());

        itemsTable.refresh();
        updateRefundLabel();
    }

    @FXML
    private void handleSave() {

        // Work out returned qty from the table
        List<Integer> returnQtys = getReturnQtys();

        String error = saleService.validateReturns(soldItems, returnQtys);
        if (error != null) {
            showAlert(Alert.AlertType.WARNING, "Invalid Return", error);
            return;
        }

        double refundAmount = saleService.calculateRefund(soldItems, returnQtys);

        // Confirm refund
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Refund");
        confirm.setHeaderText(null);
        confirm.setContentText("Refund Rs. " + String.format("%.2f", refundAmount)
                + " for invoice #" + saleId + "?");

        Optional<ButtonType> confirmResult = confirm.showAndWait();
        if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
            return;
        }

        // Save
        try {
            saleService.returnItems(
                    saleId,
                    soldItems,
                    returnQtys,
                    UserService.getLoggedInUserId()
            );

            showAlert(Alert.AlertType.INFORMATION, "Sale Updated",
                    "Invoice #" + saleId + " updated.\nRefund: Rs. "
                            + String.format("%.2f", refundAmount));

            saved = true;
            closeWindow();

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Update Failed",
                    "Nothing was changed:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    // qty the customer bought of this item
    private int getBoughtQty(Item item) {
        for (Item sold : soldItems) {
            if (sold.getProductId().equals(item.getProductId())) {
                return sold.getQuantity();
            }
        }
        return 0;
    }

    // how many of each sold item the customer gave back
    private List<Integer> getReturnQtys() {

        List<Integer> returnQtys = new ArrayList<>();

        for (Item sold : soldItems) {

            int keptQty = 0;

            for (Item kept : keptItems) {
                if (kept.getProductId().equals(sold.getProductId())) {
                    keptQty = kept.getQuantity();
                }
            }

            returnQtys.add(sold.getQuantity() - keptQty);
        }

        return returnQtys;
    }

    private void updateRefundLabel() {
        double refund = saleService.calculateRefund(soldItems, getReturnQtys());

        refundLabel.setText("Refund: Rs. " + String.format("%.2f", refund));
    }

    private void closeWindow() {
        Stage stage = (Stage) itemsTable.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
