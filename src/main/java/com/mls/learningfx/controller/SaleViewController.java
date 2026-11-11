
package com.mls.learningfx.controller;

import com.mls.learningfx.model.Customer;
import com.mls.learningfx.model.Item;
import com.mls.learningfx.model.Product;
import com.mls.learningfx.model.Supplier;
import com.mls.learningfx.repository.BaseRepository;
import com.mls.learningfx.service.ProductService;
import com.mls.learningfx.service.SaleReportService;
import com.mls.learningfx.service.UserService;
import com.mls.learningfx.util.NavigationUtil;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    private TableColumn<Item, Void> actionColumn;

    @FXML
    public Button searchButton;

    @FXML
    public TextField searchField;

    @FXML
    public TextField barcodeTextField;

    @FXML
    public Label grandTotalLabel;


    private final ProductService productService = new ProductService();
    private final SaleReportService saleReportService = new SaleReportService();

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


        addItemActionButtons();
        getAllProducts();

//        searchField.textProperty().addListener(
//                (observable, oldValue, newValue) -> handleSearch()
//        );

        barCodeScan();


    }


    @FXML
    private void addItemActionButtons() {

        actionColumn.setCellFactory(column -> new TableCell<Item, Void>() {

            private final Button increaseBtn = new Button("+");
            private final Button decreaseBtn = new Button("-");
            private final Button deleteBtn = new Button("Delete");

            private final HBox buttons = new HBox(
                    5, decreaseBtn, increaseBtn, deleteBtn
            );

            {
                increaseBtn.setOnAction(event -> {
                    Item item = getTableView().getItems().get(getIndex());
                    increaseQuantity(item);
                });

                decreaseBtn.setOnAction(event -> {
                    Item item = getTableView().getItems().get(getIndex());
                    decreaseQuantity(item);
                });

                deleteBtn.setOnAction(event -> {
                    Item item = getTableView().getItems().get(getIndex());
                    itemObservableList.remove(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                setGraphic(empty ? null : buttons);
            }
        });
    }


    private void increaseQuantity(Item item) {

        Product product = productTable.getItems()
                .stream()
                .filter(p -> p.getId() == item.getProductId())
                .findFirst()
                .orElse(null);

        if (product == null || item.getQuantity() >= product.getQuantity()) {
            showAlert(Alert.AlertType.WARNING,
                    "Stock Limit", "No more stock available.");
            return;
        }

        item.setQuantity(item.getQuantity() + 1);

        item.setSubTotal(item.getQuantity() * item.getPrice());

        refreshTable();
    }

    private void refreshTable() {
        Double sum = 0.0;
        for(Item item : selectedItemsTable.getItems()){
            sum += item.getSubTotal();

        }

        grandTotalLabel.setText(sum.toString());
        selectedItemsTable.refresh();

    }

    private void decreaseQuantity(Item item) {

        if (item.getQuantity() > 1) {

            item.setQuantity(item.getQuantity() - 1);

            item.setSubTotal(item.getQuantity() * item.getPrice());

        } else {

            itemObservableList.remove(item);
        }

        refreshTable();
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


                    addOrUpdateIntoItemList(product);
                    // Process scanned barcode
                    //searchProductByBarcode(barcode);

                    // Clear TextField
                    barcodeTextField.clear();

                    // Focus again
                    barcodeTextField.requestFocus();
                }
            });

            pause.playFromStart();
        });
    }

    private void addOrUpdateIntoItemList(Product product) {

        Item foundItem = selectedItemsTable.getItems()
                .stream()
                .filter(i->i.getProductId().equals(product.getId()))
                .findFirst().orElse(null);

        if(foundItem !=null){
            foundItem.setQuantity(foundItem.getQuantity() + 1);
            foundItem.setSubTotal(foundItem.getQuantity() * foundItem.getPrice());
        }else{
            Item item = Item.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .quantity(1)
                    .price(product.getSellPrice())
                    .subTotal(product.getSellPrice())
                    .build();

            itemObservableList.add(item);

        }

        selectedItemsTable.setItems(itemObservableList);
        selectedItemsTable.refresh();
      refreshTable();

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

            Integer inputQty = Integer.parseInt(qty);
            //its product stock
            if(product.getQuantity() > inputQty){



                Item itemFound = itemObservableList.stream()
                        .filter(i->i.getProductId().equals(product.getId()))
                        .findFirst().orElse(null);
                if(itemFound != null){
                    itemFound.setQuantity(itemFound.getQuantity() + inputQty);
                    itemFound.setSubTotal(itemFound.getQuantity() * product.getSellPrice());

                }else{
                    Item item = Item.builder()
                            .productId(product.getId())
                            .name(product.getName())
                            .price(product.getSellPrice())
                            .quantity(inputQty)
                            .subTotal(inputQty * product.getSellPrice())
                            .build();

                    itemObservableList.add(item);
                }



            }

            selectedItemsTable.setItems(itemObservableList);
            refreshTable();

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

        if (itemObservableList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Items",
                    "Add at least one item to the sale before checkout.");
            return;
        }

        // 1. Ask Customer Phone Number
        TextInputDialog phoneDialog = new TextInputDialog();
        phoneDialog.setTitle("Customer Phone");
        phoneDialog.setHeaderText("Enter Customer Phone Number");
        phoneDialog.setContentText("Phone / Contact:");

        Optional<String> phoneResult = phoneDialog.showAndWait();
        if (phoneResult.isEmpty()) {
            return;
        }

        String phone = phoneResult.get().trim();
        if (phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Customer phone number cannot be empty.");
            return;
        }

        // 2. Check if customer phone exists in DB; if not, ask Name & Contact to create new customer
        Integer customerId;
        Customer newCustomerToCreate = null;
        try {
            customerId = findCustomerIdByPhone(phone);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to search customer: " + e.getMessage());
            return;
        }

        if (customerId == null) {
            newCustomerToCreate = promptNewCustomerDialog(phone);
            if (newCustomerToCreate == null) {
                return;
            }
        }

        // 3. Ask Payment Type (online or Cash)
        ChoiceDialog<String> paymentDialog = new ChoiceDialog<>("Cash", "Cash", "online");
        paymentDialog.setTitle("Payment Type");
        paymentDialog.setHeaderText("Select Payment Method");
        paymentDialog.setContentText("Payment Type (online or Cash):");

        Optional<String> paymentResult = paymentDialog.showAndWait();
        if (paymentResult.isEmpty()) {
            return;
        }
        String selectedPaymentMode = paymentResult.get();

        // 4. Calculate total sale price
        double totalPrice = itemObservableList.stream()
                .mapToDouble(Item::getSubTotal)
                .sum();

        // 5. Execute 3-Step Checkout inside SQL Transaction (BEGIN -> COMMIT / ROLLBACK)
        Connection conn = null;
        try {
            conn = getDbConnection();
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            // Create customer if not already present in database
            if (customerId == null) {
                customerId = insertCustomer(conn, newCustomerToCreate.getName(), newCustomerToCreate.getContact());
            }

            // Get payment_type_id from payment_type table
            int paymentTypeId = getOrCreatePaymentTypeId(conn, selectedPaymentMode);

            // STEP 1: Insert into sale table (s_id auto-generated, date = now)
            int saleId = insertSale(conn, customerId, UserService.getLoggedInUserId(), totalPrice, paymentTypeId);

            // STEP 2: Insert products into sale_item table
            insertSaleItems(conn, saleId, itemObservableList);

            // STEP 3: Update stock table (deduct sold quantities)
            updateStockForItems(conn, itemObservableList);

            conn.commit(); // COMMIT TRANSACTION

            showAlert(Alert.AlertType.INFORMATION, "Checkout Successful",
                    "Sale completed successfully!\nSale ID: " + saleId
                            + "\nPayment Mode: " + selectedPaymentMode
                            + "\nTotal Price: Rs. " + String.format("%.2f", totalPrice));

            itemObservableList.clear();
            selectedItemsTable.refresh();
            getAllProducts();

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // ROLLBACK TRANSACTION if any step fails
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            showAlert(Alert.AlertType.ERROR, "Checkout Failed",
                    "Transaction rolled back due to an error:\n" + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    private Connection getDbConnection() {
        return new BaseRepository() {
            Connection open() {
                return this.connection;
            }
        }.open();
    }

    private Integer findCustomerIdByPhone(String phone) throws SQLException {
        String sql = "SELECT id FROM customer WHERE contact = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }

    private Customer promptNewCustomerDialog(String initialPhone) {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("New Customer");
        dialog.setHeaderText("Customer phone not found in database.\nEnter details to create customer:");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Enter customer name");

        TextField contactField = new TextField(initialPhone);
        contactField.setEditable(false);
        contactField.setDisable(true);

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
                return new Customer(null, nameField.getText().trim(), initialPhone);
            }
            return null;
        });

        Optional<Customer> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }

        Customer customer = result.get();
        if (customer.getName().isEmpty() || customer.getContact().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Customer Name and Contact cannot be empty!");
            return null;
        }

        return customer;
    }

    private int insertCustomer(Connection conn, String name, String contact) throws SQLException {
        String sql = "INSERT INTO customer (name, contact) VALUES (?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, contact);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        throw new SQLException("Failed to create customer record.");
    }

    private int getOrCreatePaymentTypeId(Connection conn, String mode) throws SQLException {
        String selectSql = "SELECT id FROM payment_type WHERE LOWER(mode) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, mode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        String insertSql = "INSERT INTO payment_type (mode) VALUES (?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, mode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        throw new SQLException("Failed to resolve payment_type_id for mode: " + mode);
    }

    private int insertSale(Connection conn, int customerId, Integer userId, double totalPrice, int paymentTypeId) throws SQLException {
        String sql = "INSERT INTO sale (cus_id, user_id, date, total_price, payment_type_id) VALUES (?, ?, ?, ?, ?) RETURNING s_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setObject(2, userId, Types.INTEGER);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBigDecimal(4, BigDecimal.valueOf(totalPrice));
            ps.setInt(5, paymentTypeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("s_id");
                }
            }
        }
        throw new SQLException("Failed to insert sale record.");
    }

    private void insertSaleItems(Connection conn, int saleId, List<Item> items) throws SQLException {
        String sql = "INSERT INTO sale_item (s_id, p_id, qty) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Item item : items) {
                ps.setInt(1, saleId);
                ps.setInt(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Failed to insert sale_item for product: " + item.getName());
                }
            }
        }
    }

    private void updateStockForItems(Connection conn, List<Item> items) throws SQLException {
        String sql = "UPDATE stock SET qty = qty - ?, date = ? WHERE product_id = ?";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Item item : items) {
                ps.setInt(1, item.getQuantity());
                ps.setTimestamp(2, now);
                ps.setInt(3, item.getProductId());
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Stock record not found for product: " + item.getName());
                }
            }
        }
    }

    @FXML
    private void handleReport(ActionEvent event) {

        if (itemObservableList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Items",
                    "Add at least one item to the sale before creating a report.");
            return;
        }

        try {
            Path pdfPath = saleReportService.generateSaleReport(itemObservableList);
            showAlert(Alert.AlertType.INFORMATION, "Report Created",
                    "PDF saved to:\n" + pdfPath);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Report Failed",
                    "Could not create PDF: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}