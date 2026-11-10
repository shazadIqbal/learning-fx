
package com.mls.learningfx.controller;

import com.mls.learningfx.model.Item;
import com.mls.learningfx.model.Product;
import com.mls.learningfx.model.Supplier;
import com.mls.learningfx.service.ProductService;
import com.mls.learningfx.service.SaleReportService;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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

        selectedItemsTable.refresh();
    }

    private void decreaseQuantity(Item item) {

        if (item.getQuantity() > 1) {

            item.setQuantity(item.getQuantity() - 1);

            item.setSubTotal(item.getQuantity() * item.getPrice());

        } else {

            itemObservableList.remove(item);
        }

        selectedItemsTable.refresh();
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
            selectedItemsTable.refresh();

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