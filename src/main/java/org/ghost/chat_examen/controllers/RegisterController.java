
package org.ghost.chat_examen.controllers;


import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label messageLabel;
    @FXML private Label usernameHint;
    @FXML private Button registerBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Remplir le ComboBox avec les rôles disponibles
        roleComboBox.setItems(FXCollections.observableArrayList(
                "MEMBRE", "BENEVOLE", "ORGANISATEUR"
        ));
        roleComboBox.getSelectionModel().selectFirst(); // MEMBRE par défaut

        // Vérification username en temps réel
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 3 && !newVal.isEmpty()) {
                usernameHint.setText("Minimum 3 caractères");
            } else if (newVal.contains(" ")) {
                usernameHint.setText("Pas d'espaces autorisés");
            } else {
                usernameHint.setText("");
            }
        });
    }

    // Inscription
    @FXML
    public void sInscrire() {
        //A faire plus tard
    }


    // Retour a la page de login
    @FXML
    public void retourLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/ghost/chat_examen/login-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 900, 520));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Utilitaires
    private void afficherErreur(String message) {
        messageLabel.getStyleClass().remove("success-label");
        messageLabel.getStyleClass().add("error-label");
        messageLabel.setText("⚠ " + message);
    }

    private void afficherSucces(String message) {
        messageLabel.getStyleClass().remove("error-label");
        messageLabel.getStyleClass().add("success-label");
        messageLabel.setText("✅ " + message);
    }
}