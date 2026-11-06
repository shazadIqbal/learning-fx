package com.mls.learningfx.controller;

import com.mls.learningfx.util.NavigationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

import static com.mls.learningfx.util.NavigationUtil.*;

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
    private Button logoutBtn;

   @FXML
    public void logout() {
       Stage stage = (Stage) logoutBtn.getScene().getWindow();
       try {
           NavigationUtil.navigate(stage,NavigationUtil.LOGIN);

       } catch (IOException e) {
           throw new RuntimeException(e);
       }

   }
}
