package org.ghost.chat_examen.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.ghost.chat_examen.client.ClientSocket;

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
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmPasswordField.getText();
        String role     = roleComboBox.getValue();

        // Validations côté client
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        if (username.length() < 3) {
            afficherErreur("Le nom d'utilisateur doit faire au moins 3 caractères.");
            return;
        }

        if (username.contains(" ")) {
            afficherErreur("Le nom d'utilisateur ne doit pas contenir d'espaces.");
            return;
        }

        if (password.length() < 6) {
            afficherErreur("Le mot de passe doit faire au moins 6 caractères.");
            return;
        }

        if (!password.equals(confirm)) {
            afficherErreur("Les mots de passe ne correspondent pas.");
            return;
        }

        registerBtn.setDisable(true);
        registerBtn.setText("Création...");

        // Dans un thread separe pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                // Se connecter au serveur si pas encore connecte
                if (!ClientSocket.estConnecte()) {
                    ClientSocket.connecter();
                }

                ClientSocket.envoyer("REGISTER " + username + " " + password + " " + role);
                String reponse = ClientSocket.lire();

                Platform.runLater(() -> {
                    if (reponse != null && reponse.equals("REGISTER_OK")) {
                        afficherSucces("Compte créé ! Redirection...");

                        // Attendre un peu puis retourner au login
                        new Thread(() -> {
                            try {
                                Thread.sleep(1500);
                                Platform.runLater(this::retourLogin);
                            } catch (InterruptedException ignored) {}
                        }).start();

                    } else if (reponse != null && reponse.startsWith("REGISTER_FAIL")) {
                        String raison = reponse.replace("REGISTER_FAIL ", "");
                        afficherErreur(raison);
                        registerBtn.setDisable(false);
                        registerBtn.setText("Créer mon compte");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    afficherErreur("Impossible de joindre le serveur.");
                    registerBtn.setDisable(false);
                    registerBtn.setText("Créer mon compte");
                });
            }
        }).start();
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