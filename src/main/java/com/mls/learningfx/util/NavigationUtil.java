package com.mls.learningfx.util;

import com.mls.learningfx.HelloApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationUtil {


    public static final String DASHBOARD = "DASHBOARD";


    public static void navigate(Stage stage,String action) throws IOException {


        switch (action){

            case DASHBOARD:
                FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class
                        .getResource("dashboard-view.fxml"));

                // scene with ui (fxml)
                Scene scene = new Scene(fxmlLoader.load(), 600, 600);
                stage.setScene(scene);
                stage.show();
                break;


        }
    }
}
