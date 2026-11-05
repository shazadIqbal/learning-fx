package com.mls.learningfx.controller;

import com.mls.learningfx.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ProductViewController {
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Number> idColumn;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, String> categoryColumn;
    @FXML
    private TableColumn<Product, Number> priceColumn;
    @FXML
    private TableColumn<Product, Number> sellPriceColumn;
    @FXML
    private TableColumn<Product, String> createdByColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Label statusLabel;
}
