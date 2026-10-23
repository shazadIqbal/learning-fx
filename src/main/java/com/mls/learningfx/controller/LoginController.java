package com.mls.learningfx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.w3c.dom.Text;

public class LoginController {
    @FXML
    private TextField username;
    @FXML
    private TextField password;


    @FXML
    public void sayLogin(){
        System.out.println(username.getText());
        System.out.println(password.getText());
    }
}
