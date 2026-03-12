package org.ghost.chat_examen.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.ghost.chat_examen.client.ClientSocket;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;


    @FXML
    public void seConnecter() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        loginBtn.setDisable(true);
        loginBtn.setText("Connexion...");

        new Thread(() -> {
            try {
                ClientSocket.connecter();
                ClientSocket.envoyer("LOGIN " + username + " " + password);
                String reponse = ClientSocket.lire();

                Platform.runLater(() -> {
                    if (reponse != null && reponse.startsWith("LOGIN_OK")) {
                        String role = reponse.split(" ")[1];
                        ouvrirContacts(username, role);
                    } else {
                        afficherErreur("Identifiants incorrects.");
                        loginBtn.setDisable(false);
                        loginBtn.setText("Se connecter");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    afficherErreur("Impossible de joindre le serveur.");
                    loginBtn.setDisable(false);
                    loginBtn.setText("Se connecter");
                });
            }
        }).start();
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

    private void ouvrirContacts(String username, String role) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/ghost/chat_examen/contacts-view.fxml"));

            // Récuperer le stage depuis le champ FXML
            Stage stage = (Stage) usernameField.getScene().getWindow();

            Scene scene = new Scene(loader.load(), 1000, 650);

            ContactsController controller = loader.getController();
            controller.setStage(stage);        // passer le stage
            controller.initialiser(username, role);

            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afficherErreur(String message) {
        errorLabel.setText(message);
    }
}