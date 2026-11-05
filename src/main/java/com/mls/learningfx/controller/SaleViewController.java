package com.mls.learningfx.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class SaleViewController {

    @FXML
    public void handleSearch(){

    }

    @FXML
    private void handleProductClick(MouseEvent event) {

        if (event.getClickCount() != 2) {
            return;
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {

        // TODO: Navigate back to previous screen.
    }

    @FXML
    private void handleCheckout(ActionEvent event) {

    }
}
