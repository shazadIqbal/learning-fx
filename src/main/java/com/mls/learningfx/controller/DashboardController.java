package com.mls.learningfx.controller;

import com.mls.learningfx.service.ProductService;
import com.mls.learningfx.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

import static com.mls.learningfx.util.NavigationUtil.*;

public class DashboardController {

    @FXML
    private Button productBtn;
    @FXML
    private Button supplierBtn;
    @FXML
    private Button reportBtn;

    @FXML
    private Button logoutBtn;
    @FXML
    private Button customerBtn;
    @FXML
    private Button expiryReportBtn;
    @FXML
    private Button notificationBtn;
    @FXML
    private Label notificationBadge;

    private final ProductService productService = new ProductService();

    @FXML
    public void initialize() {
        loadNotifications();
    }

    @FXML
    public void productView(){
        Stage stage = (Stage) productBtn.getScene().getWindow();
        try {
            NavigationUtil.navigate(stage,PRODUCT_VIEW);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @FXML
    public void supplierView(){
        Stage stage = (Stage) supplierBtn.getScene().getWindow();
        try {
            NavigationUtil.navigate(stage,SUPPLIER_VIEW);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    public void reportView(){
        Stage stage = (Stage) reportBtn.getScene().getWindow();
        try {
            NavigationUtil.navigate(stage,REPORT_VIEW);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    public void onSaleClick(){
        Stage stage = (Stage) productBtn.getScene().getWindow();

        try {
            NavigationUtil.navigate(stage,SALE_VIEW);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    public void customerView(){
        Stage stage = (Stage) customerBtn.getScene().getWindow();
        try {
            NavigationUtil.navigate(stage,CUSTOMER_VIEW);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @FXML
    public void logout() {
        Stage stage = (Stage) logoutBtn.getScene().getWindow();
        try {
            NavigationUtil.navigate(stage,NavigationUtil.LOGIN);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    public void expiryReportView() {
        Stage stage = (Stage) expiryReportBtn.getScene().getWindow();
        try {
            NavigationUtil.navigate(stage, EXPIRY_REPORT_VIEW);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadNotifications() {
        try {
            List<String> notifications = productService.getExpiryNotifications(30);
            if (!notifications.isEmpty()) {
                notificationBadge.setText(String.valueOf(notifications.size()));
                notificationBadge.setVisible(true);
            } else {
                notificationBadge.setVisible(false);
            }
        } catch (Exception e) {
            notificationBadge.setVisible(false);
        }
    }

    @FXML
    public void onNotificationClick() {
        List<String> notifications = productService.getExpiryNotifications(30);

        Stage popupStage = new Stage();
        popupStage.setTitle("Notifications - Expiry Alerts");
        popupStage.initOwner(notificationBtn.getScene().getWindow());
        popupStage.initModality(Modality.APPLICATION_MODAL);

        Label titleLabel = new Label("Product Expiry Notifications (" + notifications.size() + ")");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #172033;");

        ListView<String> listView = new ListView<>(FXCollections.observableArrayList(notifications));
        listView.setPlaceholder(new Label("No expired or near-expiry products found."));
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("EXPIRED:")) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-padding: 8;");
                    } else {
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold; -fx-padding: 8;");
                    }
                }
            }
        });

        Button openReportBtn = new Button("Open Expiry Report");
        openReportBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 16 8 16;");
        openReportBtn.setOnAction(e -> {
            popupStage.close();
            expiryReportView();
        });

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 16 8 16;");
        closeBtn.setOnAction(e -> popupStage.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        HBox buttonBar = new HBox(10, spacer, openReportBtn, closeBtn);

        VBox root = new VBox(14, titleLabel, listView, buttonBar);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f6f9;");

        popupStage.setScene(new Scene(root, 540, 380));
        popupStage.showAndWait();
    }
}

