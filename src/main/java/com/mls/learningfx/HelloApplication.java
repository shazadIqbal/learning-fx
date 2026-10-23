package com.mls.learningfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // load the ui -- fxml
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class
                .getResource("login-view.fxml"));

        // scene with ui (fxml)
        Scene scene = new Scene(fxmlLoader.load(), 520, 540);
        // stage title
        stage.setTitle("Stepway!");
        // set the scean into stage
        stage.setScene(scene);
        // show means view
        stage.show();
    }
}
