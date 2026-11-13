package com.mls.learningfx.util;

import com.mls.learningfx.HelloApplication;
import com.mls.learningfx.controller.CustomerHistoryController;
import com.mls.learningfx.model.Customer;
import javafx.fxml.FXMLLoader;
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
    public static final String CUSTOMER_HISTORY_VIEW = "CUSTOMER_HISTORY_VIEW";





    public static void navigate(Stage stage,String action,Object obj) throws IOException {

        switch (action){

            case CUSTOMER_HISTORY_VIEW:
                Customer customer = (Customer) obj;
                FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class
                        .getResource("customer-history-view.fxml"));


            // Pass selected customer



                // scene with ui (fxml)
                Scene scene = new Scene(fxmlLoader.load(), 800, 700);
                stage.setScene(scene);
                stage.setMaximized(false);
                CustomerHistoryController controller = fxmlLoader.getController();
                controller.setCustomer(customer);

                stage.show();

                break;
        }
    }

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
                loadView(stage,"sale-view.fxml");
                stage.setMaximized(true);
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
            case CUSTOMER_HISTORY_VIEW:
                loadView(stage, "customer-history-view.fxml");
                break;
        }
    }

    private static void loadView(Stage stage,String path) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class
                .getResource(path));
        stage.setMaximized(false);

        // scene with ui (fxml)
        Scene scene = new Scene(fxmlLoader.load(), 800, 700);
        stage.setScene(scene);
        stage.show();

    }

}
