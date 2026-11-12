package com.mls.learningfx.controller;

import com.mls.learningfx.model.Product;
import com.mls.learningfx.model.Supplier;
import com.mls.learningfx.service.ProductService;
import com.mls.learningfx.service.SupplierService;
import com.mls.learningfx.service.UserService;
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
import java.util.LinkedHashMap;
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
    private TableColumn<Product, String> barcodeColumn;

    @FXML
    private TableColumn<Product, String> categoryColumn;

    @FXML
    private TableColumn<Product, Double> priceColumn;

    @FXML
    private TableColumn<Product, Double> sellPriceColumn;

    @FXML
    private TableColumn<Product, Integer> stockColumn;

    @FXML
    private TableColumn<Product, String> createdByColumn;

    @FXML
    private TableColumn<Product, Product> actionColumn;

    @FXML
    private TextField searchField;

    @FXML
    private TextField barcodeField;

    @FXML
    private Button backButton;

    @FXML
    private Button addProductButton;

    @FXML
    private Button updateStockButton;

    private final ProductService productService = new ProductService();

    private final SupplierService supplierService = new SupplierService();


    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        barcodeColumn.setCellValueFactory(
                new PropertyValueFactory<>("barcode")
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

        stockColumn.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        createdByColumn.setCellValueFactory(
                new PropertyValueFactory<>("createdByName")
        );

        addEditDeleteButtons();

        getAllProducts();

        addProductButton.setOnAction(e -> addProduct());

        updateStockButton.setOnAction(e -> updateStock());

        backButton.setOnAction(e -> backToDashboard());

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> searchProducts()
        );

        barcodeField.textProperty().addListener(
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

        ObservableList<Product> result;

        if (keyword == null || keyword.trim().isEmpty()) {
            result = productService.getAllProducts();
        } else {
            result = productService.searchProducts(keyword);
        }

        String barcode = barcodeField.getText().trim();

        if (!barcode.isEmpty()) {
            result.removeIf(product -> product.getBarcode() == null
                    || !product.getBarcode().toLowerCase()
                    .contains(barcode.toLowerCase()));
        }

        productTable.setItems(result);
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


    private void updateStock() {

        Product product =
                productTable.getSelectionModel().getSelectedItem();

        if (product == null) {
            showError("Please select a product first");
            return;
        }

        ComboBox<Integer> supplierBox = createSupplierDropdown();

        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity to add");

        VBox vbox = new VBox(10);

        vbox.setPadding(new Insets(20));
        vbox.setPrefWidth(320);

        vbox.getChildren().addAll(
                new Label("Product: " + product.getName()),
                new Label("Current Stock: " + product.getQuantity()),

                new Label("Quantity to Add"),
                qtyField,

                new Label("Supplier"),
                supplierBox
        );

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Update Stock");
        dialog.getDialogPane().setContent(vbox);

        ButtonType saveButton = new ButtonType(
                "Save",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                ButtonType.CANCEL
        );

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButton) {

            try {

                int qty = Integer.parseInt(qtyField.getText().trim());

                if (qty <= 0) {
                    showError("Quantity must be more than 0");
                    return;
                }

                if (supplierBox.getValue() == null) {
                    showError("Please select a supplier");
                    return;
                }

                boolean saved = productService.addStock(
                        product.getId(),
                        qty,
                        supplierBox.getValue(),
                        UserService.getLoggedInUserId()
                );

                if (saved) {
                    showSuccess("Stock updated successfully");
                    searchProducts();
                } else {
                    showError("Something went wrong");
                }

            } catch (NumberFormatException e) {

                showError("Please enter a whole number for quantity");

            } catch (RuntimeException e) {

                showError("Could not update stock: " + e.getMessage());
            }
        }
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


    private ComboBox<Integer> createSupplierDropdown() {

        // id and name of each supplier
        Map<Integer, String> suppliers = new LinkedHashMap<>();

        for (Supplier supplier : supplierService.getAllSuppliers()) {
            suppliers.put(supplier.getId(), supplier.getName());
        }

        ComboBox<Integer> supplierBox = createDropdown(suppliers);
        supplierBox.setPromptText("Select Supplier");

        return supplierBox;
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
        TextField barcodeInputField = new TextField();

        // for new product only
        TextField stockField = new TextField();
        stockField.setPromptText("Leave empty for 0");

        ComboBox<Integer> supplierBox = createSupplierDropdown();

        Map<Integer, String> categories =
                productService.getAllCategories();

        ComboBox<Integer> categoryBox =
                createDropdown(categories);

        categoryBox.setPromptText("Select Category");

        if (isEdit) {

            nameField.setText(product.getName());

            priceField.setText(
                    String.valueOf(product.getPrice())
            );

            sellPriceField.setText(
                    String.valueOf(product.getSellPrice())
            );

            categoryBox.setValue(product.getCategoryId());
            barcodeInputField.setText(product.getBarcode());
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

                new Label("Barcode"),
                barcodeInputField
        );

        if (!isEdit) {
            vbox.getChildren().addAll(
                    new Label("Stock"),
                    stockField,

                    new Label("Supplier (needed if stock is more than 0)"),
                    supplierBox
            );
        }

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
                String barcode = barcodeInputField.getText().trim();

                if (barcode.isEmpty()) {
                    barcode = null;
                }

                if (name.isEmpty()
                        || categoryBox.getValue() == null) {

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

                // empty stock means 0
                int stock = 0;
                String stockText = stockField.getText().trim();

                if (!isEdit && !stockText.isEmpty()) {

                    try {
                        stock = Integer.parseInt(stockText);
                    } catch (NumberFormatException e) {
                        showError("Please enter a whole number for stock");
                        return;
                    }

                    if (stock < 0) {
                        showError("Stock cannot be negative");
                        return;
                    }

                    if (stock > 0 && supplierBox.getValue() == null) {
                        showError("Please select a supplier for the stock");
                        return;
                    }
                }

                boolean saved;

                if (isEdit) {

                    saved = productService.updateProduct(
                            product.getId(),
                            name,
                            categoryBox.getValue(),
                            price,
                            sellPrice,
                            barcode
                    );

                } else {

                    Integer loggedInUserId = UserService.getLoggedInUserId();

                    if (loggedInUserId == null) {
                        showError("Please log in before adding products");
                        return;
                    }

                    Integer newProductId = productService.addProduct(
                            name,
                            categoryBox.getValue(),
                            price,
                            sellPrice,
                            loggedInUserId,
                            barcode
                    );

                    saved = newProductId != null;

                    // add stock for new product
                    if (saved && stock > 0) {
                        saved = productService.addStock(
                                newProductId,
                                stock,
                                supplierBox.getValue(),
                                loggedInUserId
                        );
                    }
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

                showError("Could not delete this product: " +
                        e.getMessage());
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
