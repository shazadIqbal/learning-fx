
package com.mls.learningfx.controller;

import com.mls.learningfx.model.Product;
import com.mls.learningfx.service.ProductService;
import com.mls.learningfx.util.NavigationUtil;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class ProductViewController {

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, Integer> idColumn;

    @FXML
    private TableColumn<Product, String> nameColumn;

    @FXML
    private TableColumn<Product, String> categoryColumn;

    @FXML
    private TableColumn<Product, Double> priceColumn;

    @FXML
    private TableColumn<Product, Double> sellPriceColumn;

    @FXML
    private TableColumn<Product, String> createdByColumn;

    @FXML
    private TableColumn<Product, Product> actionColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button backButton;

    @FXML
    private Button addProductButton;

    private final ProductService productService = new ProductService();


    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        categoryColumn.setCellValueFactory(
                new PropertyValueFactory<>("categoryName")
        );

        priceColumn.setCellValueFactory(
                new PropertyValueFactory<>("price")
        );

        sellPriceColumn.setCellValueFactory(
                new PropertyValueFactory<>("sellPrice")
        );

        createdByColumn.setCellValueFactory(
                new PropertyValueFactory<>("createdByName")
        );

        addEditDeleteButtons();

        getAllProducts();

        addProductButton.setOnAction(e -> addProduct());

        backButton.setOnAction(e -> backToDashboard());

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> searchProducts()
        );
    }


    public void getAllProducts() {

        ObservableList<Product> result =
                productService.getAllProducts();

        productTable.setItems(result);
    }


    private void searchProducts() {

        String keyword = searchField.getText();

        if (keyword == null || keyword.trim().isEmpty()) {
            getAllProducts();
        } else {
            productTable.setItems(
                    productService.searchProducts(keyword)
            );
        }
    }


    private void addEditDeleteButtons() {

        actionColumn.setCellValueFactory(
                cellData -> new ReadOnlyObjectWrapper<>(
                        cellData.getValue()
                )
        );

        actionColumn.setCellFactory(col -> new TableCell<Product, Product>() {

            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            private final HBox box = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setOnAction(e -> {

                    Product product = getItem();

                    if (product != null) {
                        editProduct(product);
                    }
                });

                deleteBtn.setOnAction(e -> {

                    Product product = getItem();

                    if (product != null) {
                        deleteProduct(product);
                    }
                });
            }

            @Override
            protected void updateItem(Product product, boolean empty) {

                super.updateItem(product, empty);

                if (empty || product == null) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
    }


    private void addProduct() {
        showProductDialog(null);
    }


    private void editProduct(Product product) {
        showProductDialog(product);
    }


    private ComboBox<Integer> createDropdown(
            Map<Integer, String> items) {

        ComboBox<Integer> comboBox = new ComboBox<>();

        comboBox.getItems().addAll(items.keySet());

        comboBox.setMaxWidth(Double.MAX_VALUE);

        comboBox.setConverter(new StringConverter<Integer>() {

            @Override
            public String toString(Integer id) {

                if (id == null) {
                    return "";
                }

                return items.get(id);
            }

            @Override
            public Integer fromString(String text) {
                return null;
            }
        });

        return comboBox;
    }


    private void showProductDialog(Product product) {

        boolean isEdit = product != null;

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle(
                isEdit ? "Edit Product" : "Add Product"
        );

        TextField nameField = new TextField();
        TextField priceField = new TextField();
        TextField sellPriceField = new TextField();

        Map<Integer, String> categories =
                productService.getAllCategories();

        Map<Integer, String> users =
                productService.getAllUsers();

        ComboBox<Integer> categoryBox =
                createDropdown(categories);

        ComboBox<Integer> userBox =
                createDropdown(users);

        categoryBox.setPromptText("Select Category");
        userBox.setPromptText("Select User");

        if (isEdit) {

            nameField.setText(product.getName());

            priceField.setText(
                    String.valueOf(product.getPrice())
            );

            sellPriceField.setText(
                    String.valueOf(product.getSellPrice())
            );

            categoryBox.setValue(product.getCategoryId());
            userBox.setValue(product.getCreatedBy());
        }

        VBox vbox = new VBox(10);

        vbox.setPadding(new Insets(20));
        vbox.setPrefWidth(320);

        vbox.getChildren().addAll(
                new Label("Product Name"),
                nameField,

                new Label("Category"),
                categoryBox,

                new Label("Purchase Price"),
                priceField,

                new Label("Selling Price"),
                sellPriceField,

                new Label("Created By"),
                userBox
        );

        dialog.getDialogPane().setContent(vbox);

        ButtonType saveButton = new ButtonType(
                isEdit ? "Update" : "Add",
                ButtonBar.ButtonData.OK_DONE
        );

        ButtonType cancelButton = new ButtonType(
                "Cancel",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                cancelButton
        );

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButton) {

            try {

                String name = nameField.getText().trim();

                if (name.isEmpty()
                        || categoryBox.getValue() == null
                        || userBox.getValue() == null) {

                    showError("Please fill in all fields");
                    return;
                }

                double price = Double.parseDouble(
                        priceField.getText().trim()
                );

                double sellPrice = Double.parseDouble(
                        sellPriceField.getText().trim()
                );

                if (!Double.isFinite(price)
                        || !Double.isFinite(sellPrice)
                        || price < 0
                        || sellPrice < 0) {

                    showError("Please enter valid prices");
                    return;
                }

                boolean saved;

                if (isEdit) {

                    saved = productService.updateProduct(
                            product.getId(),
                            name,
                            categoryBox.getValue(),
                            price,
                            sellPrice,
                            userBox.getValue()
                    );

                } else {

                    saved = productService.addProduct(
                            name,
                            categoryBox.getValue(),
                            price,
                            sellPrice,
                            userBox.getValue()
                    );
                }

                if (saved) {

                    showSuccess(
                            isEdit
                                    ? "Product updated successfully"
                                    : "Product added successfully"
                    );

                    searchProducts();

                } else {

                    showError("Something went wrong");
                }

            } catch (NumberFormatException e) {

                showError("Please enter valid numeric prices");

            } catch (RuntimeException e) {

                showError("Could not save product: " +
                        e.getMessage());
            }
        }
    }


    private void deleteProduct(Product product) {

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Delete Product");
        confirmation.setHeaderText(
                "Delete " + product.getName() + "?"
        );

        confirmation.setContentText(
                "Are you sure you want to delete this product?"
        );

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent()
                && result.get() == ButtonType.OK) {

            try {

                boolean deleted =
                        productService.deleteProduct(product.getId());

                if (deleted) {

                    showSuccess("Product deleted successfully");
                    searchProducts();

                } else {

                    showError("Product could not be deleted");
                }

            } catch (RuntimeException e) {

                showError(
                        "Could not delete this product. " +
                                "It may be used in stock, sales, " +
                                "or supplier records."
                );
            }
        }
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
}
