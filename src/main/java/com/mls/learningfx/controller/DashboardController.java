package com.mls.learningfx.controller;

import com.mls.learningfx.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

import static com.mls.learningfx.util.NavigationUtil.PRODUCT_VIEW;

public class DashboardController {


    @FXML
    private Button productBtn;

    @FXML
    public void productView(){
        Stage stage = (Stage) productBtn.getScene().getWindow();
        try {
            NavigationUtil.navigate(stage,PRODUCT_VIEW);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
