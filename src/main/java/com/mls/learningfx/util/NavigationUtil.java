package com.mls.learningfx.util;

import com.mls.learningfx.HelloApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationUtil {


    public static final String DASHBOARD = "DASHBOARD";
    public static final String PRODUCT_VIEW = "PRODUCT_VIEW";
    public static final String SUPPLIER_VIEW = "SUPPLIER_VIEW";
    public static final String SALE_VIEW = "SALE_VIEW";
    public static final String REPORT_VIEW = "REPORT_VIEW";
    public static final String LOGIN ="LOGIN";
    public static final String CUSTOMER_VIEW = "CUSTOMER_VIEW";
    public static final String SALE_RECORD_VIEW = "SALE_RECORD_VIEW";

    public static void navigate(Stage stage,String action) throws IOException {


        switch (action){

            case DASHBOARD:
                loadView(stage,"dashboard-view.fxml");
                break;
            case PRODUCT_VIEW:
                loadView(stage,"product-view.fxml");
                break;
            case SUPPLIER_VIEW:
                loadView(stage, "supplier-view.fxml");
                break;
            case SALE_VIEW:
                if (stage.isMaximized() && stage.getScene() != null) {
                    FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("sale-view.fxml"));
                    stage.getScene().setRoot(fxmlLoader.load());
                } else {
                    loadView(stage,"sale-view.fxml");
                    stage.setMaximized(true);
                }
                break;
            case LOGIN:
                loadView(stage,"login-view.fxml");
                break;
            case REPORT_VIEW:
                loadView(stage,"report-view.fxml");
                break;
            case CUSTOMER_VIEW:
                loadView(stage, "customer-view.fxml");
                break;
            case SALE_RECORD_VIEW:
                if (stage.isMaximized() && stage.getScene() != null) {
                    FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("sale-record-view.fxml"));
                    stage.getScene().setRoot(fxmlLoader.load());
                } else {
                    loadView(stage, "sale-record-view.fxml");
                    stage.setMaximized(true);
                }
                break;
        }
    }

    private static void loadView(Stage stage, String path) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(path));
        Parent root = fxmlLoader.load();

        if (stage.getScene() == null) {
            stage.setScene(new Scene(root));
        } else {
            stage.getScene().setRoot(root);   // same scene, sirf content badlo
        }

        if (!stage.isMaximized()) {
            stage.setMaximized(true);
        }
        stage.show();
    }
}

