package com.mls.learningfx.controller;

import com.mls.learningfx.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

import static com.mls.learningfx.util.NavigationUtil.PRODUCT_VIEW;
import static com.mls.learningfx.util.NavigationUtil.SUPPLIER_VIEW;

public class DashboardController {


    @FXML
    private Button productBtn;
    @FXML
    private Button supplierBtn;

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
}
