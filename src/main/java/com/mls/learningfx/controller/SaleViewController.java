package com.mls.learningfx.controller;

import com.mls.learningfx.model.Item;
import com.mls.learningfx.model.Product;
import com.mls.learningfx.service.ProductService;
import com.mls.learningfx.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

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
    private TableColumn<Product, String> productCategoryColumn;

    @FXML
    private TableColumn<Product, Double> productPriceColumn;
    @FXML
    private TableColumn<Product, Integer> productStockColumn;

    @FXML
    public Button searchButton;

    @FXML
    public TextField searchField;


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
        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("Quantity ");
       // dialog.setHeaderText("Enter Quantity");
        dialog.setContentText("Enter Quantity :");

        dialog.showAndWait().ifPresent(qty -> {

            Integer inputQty =  Integer.parseInt(qty);
            //its product stock
            if(product.getQuantity() > inputQty){

                Item item = Item.builder()
                        .name(product.getName())
                        .price(product.getSellPrice())
                        .quantity(inputQty)
                        .subTotal(inputQty * product.getSellPrice())
                        .build();
                itemObservableList.add(item);

            }

            selectedItemsTable.setItems(itemObservableList);

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
