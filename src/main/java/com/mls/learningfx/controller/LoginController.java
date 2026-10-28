package com.mls.learningfx.controller;

import com.mls.learningfx.service.UserService;
import com.mls.learningfx.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField username;
    @FXML
    private TextField password;
    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    public void sayLogin(){

        Boolean userFound = userService.login(username.getText(),password.getText());

        if(userFound){
            errorLabel.setText("Login successfull");
            errorLabel.setTextFill(Paint.valueOf("Green"));
            try {
                Stage stage = (Stage) username.getScene().getWindow();
                NavigationUtil.navigate(stage, NavigationUtil.DASHBOARD);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
            errorLabel.setText("invalid credentails");
        }

    }




}
