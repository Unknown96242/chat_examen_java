
package org.ghost.chat_examen.controllers;

import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;


public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;


    @FXML
    public void seConnecter() {
        System.out.println("redirection chat");
    }

    @FXML
    public void allerInscription() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/ghost/chat_examen/register-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1000, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}