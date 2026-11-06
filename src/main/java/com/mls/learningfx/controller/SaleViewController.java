
package com.mls.learningfx.controller;

import com.mls.learningfx.model.Item;
import com.mls.learningfx.model.Product;
import com.mls.learningfx.service.ProductService;
import com.mls.learningfx.util.NavigationUtil;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

import static com.mls.learningfx.util.NavigationUtil.DASHBOARD;
import static com.mls.learningfx.util.NavigationUtil.PRODUCT_VIEW;

public class SaleViewController {

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableView<Item> selectedItemsTable;

    @FXML
    private TableColumn<Item,String> productColumn;

    @FXML
    private TableColumn<Item,Integer> quantityColumn;

    @FXML
    private TableColumn<Item,Double> priceColumn;

    @FXML
    private TableColumn<Item,Double> subTotalColumn;

    @FXML
    private TableColumn<Product, Integer> productIdColumn;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, String> productBarcodeColumn;

    @FXML
    private TableColumn<Product, String> productCategoryColumn;

    @FXML
    private TableColumn<Product, Double> productPriceColumn;

    @FXML
    private TableColumn<Product, Integer> productStockColumn;

    @FXML
    public Button searchButton;

    @FXML
    public TextField searchField;

    @FXML
    public TextField barcodeTextField;


    private final ProductService productService = new ProductService();

    ObservableList<Item> itemObservableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        productIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        productNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        productBarcodeColumn.setCellValueFactory(
                new PropertyValueFactory<>("barcode")
        );

        productCategoryColumn.setCellValueFactory(
                new PropertyValueFactory<>("categoryName")
        );

        productPriceColumn.setCellValueFactory(
                new PropertyValueFactory<>("sellPrice")
        );

        productStockColumn.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        productColumn.setCellValueFactory(
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


        getAllProducts();

//        searchField.textProperty().addListener(
//                (observable, oldValue, newValue) -> handleSearch()
//        );

        barCodeScan();

    }

    private void barCodeScan() {
        PauseTransition pause = new PauseTransition(Duration.millis(200));

        barcodeTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            pause.setOnFinished(event -> {

                String barcode = barcodeTextField.getText().trim();

                if (!barcode.isEmpty()) {
                    System.out.println(barcode);
                    Product product = productTable.getItems().stream()
                            .filter(p->p.getBarcode().equalsIgnoreCase(barcode))
                            .findFirst().orElse(null);


                    // Process scanned barcode
                    //searchProductByBarcode(barcode);

                    // Clear TextField
                    //barcodeTextField.clear();

                    // Focus again
                    //barcodeTextField.requestFocus();
                }
            });

            pause.playFromStart();
        });
    }


    private void getAllProducts() {

        ObservableList<Product> result =
                productService.getAllProducts();

        productTable.setItems(result);
    }


    @FXML
    public void handleSearch(){
        String keyword = searchField.getText();

        if (keyword == null || keyword.trim().isEmpty()) {
            getAllProducts();
        } else {
            productTable.setItems(
                    productService.searchProducts(keyword)
            );
        }
    }

    @FXML
    private void handleProductClick(MouseEvent event) {

        if (event.getClickCount() != 2) {
            return;
        }

        Product product = productTable.getSelectionModel().getSelectedItem();

        if (product == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("Quantity ");
        // dialog.setHeaderText("Enter Quantity");
        dialog.setContentText("Enter Quantity :");

        dialog.showAndWait().ifPresent(qty -> {

            try {

                Integer inputQty = Integer.parseInt(qty.trim());

                if (inputQty <= 0) {
                    new Alert(Alert.AlertType.WARNING,
                            "Please enter a quantity greater than 0.")
                            .showAndWait();
                    return;
                }

                // Check if product already exists
                for (Item item : itemObservableList) {

                    if (item.getName().equals(product.getName())) {

                        int newQuantity = item.getQuantity() + inputQty;

                        // Check available stock
                        if (newQuantity > product.getQuantity()) {

                            new Alert(Alert.AlertType.WARNING,
                                    "Not enough stock available.")
                                    .showAndWait();
                            return;
                        }

                        // Update existing item
                        item.setQuantity(newQuantity);

                        item.setSubTotal(
                                newQuantity * product.getSellPrice()
                        );

                        selectedItemsTable.refresh();

                        return;
                    }
                }

                // Check stock for a new item
                if (inputQty > product.getQuantity()) {

                    new Alert(Alert.AlertType.WARNING,
                            "Not enough stock available.")
                            .showAndWait();
                    return;
                }

                // Add new item if it does not exist
                Item item = Item.builder()
                        .name(product.getName())
                        .price(product.getSellPrice())
                        .quantity(inputQty)
                        .subTotal(inputQty * product.getSellPrice())
                        .build();

                itemObservableList.add(item);

                selectedItemsTable.setItems(itemObservableList);

            } catch (NumberFormatException e) {

                new Alert(Alert.AlertType.WARNING,
                        "Please enter a valid quantity.")
                        .showAndWait();
            }

        });

    }

    @FXML
    private void handleBack(ActionEvent event) {
        Stage stage = (Stage) searchButton.getScene().getWindow();
        try {
            NavigationUtil.navigate(stage,DASHBOARD);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleCheckout(ActionEvent event) {

    }
}
