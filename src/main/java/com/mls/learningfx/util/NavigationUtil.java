package com.mls.learningfx.util;

import com.mls.learningfx.HelloApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationUtil {


    public static final String DASHBOARD = "DASHBOARD";
    public static final String PRODUCT_VIEW = "PRODUCT_VIEW";
    public static final String SUPPLIER_VIEW = "SUPPLIER_VIEW";
    public static final String SALE_VIEW = "SALE_VIEW";
    public static final String LOGIN ="LOGIN";

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

        }
    }

    private static void loadView(Stage stage,String path) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class
                .getResource(path));
        stage.setMaximized(false);

        // scene with ui (fxml)
        Scene scene = new Scene(fxmlLoader.load(), 600, 600);
        stage.setScene(scene);
        stage.show();
    }
}
