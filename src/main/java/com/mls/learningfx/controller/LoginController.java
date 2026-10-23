package com.mls.learningfx.controller;

import com.mls.learningfx.model.User;
import com.mls.learningfx.repository.UserRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import org.w3c.dom.Text;

import java.util.List;

public class LoginController {
    @FXML
    private TextField username;
    @FXML
    private TextField password;
    @FXML
    private Label errorLabel;

    private final UserRepository userRepository = new UserRepository();

    @FXML
    public void sayLogin(){
        System.out.println(username.getText());
        System.out.println(password.getText());

        User user = userRepository.login(username.getText(),password.getText());
        if(user != null){
            errorLabel.setText("Login successfull");
            errorLabel.setTextFill(Paint.valueOf("Green"));
        }else{
            errorLabel.setText("invalid credentails");
        }

    }
}
