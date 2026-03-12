package org.ghost.chat_examen.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Setter;
import org.ghost.chat_examen.client.ClientSocket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsController {

    @FXML private Label usernameLabel;
    @FXML private Label avatarLabel;
    @FXML private Label roleLabel;
    @FXML private Label onlineCountLabel;
    @FXML private Label totalCountLabel;
    @FXML private ListView<HBox> membresListView;
    @FXML private TextField searchField;
    @FXML private Button tabOnline;
    @FXML private Button tabConversations;
    @FXML private Button tabTous;

    // Zone chat intégrée
    @FXML private VBox welcomePane;
    @FXML private VBox chatPane;
    @FXML private Label chatDestinataireLabel;
    @FXML private Label chatAvatarLabel;
    @FXML private StackPane chatStatusDot;
    @FXML private Label chatStatusLabel;
    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageField;

    private String username;
    private String role;
    @Setter
    private Stage stage;
    private String destinataireActif = null;
    private boolean affichageTous = false;

    private final List<String> membresEnLigne       = new ArrayList<>();
    private final List<String> tousLesMembres       = new ArrayList<>();
    private final List<String> conversationsNonLues = new ArrayList<>();
    private final Map<String, Integer> notifications = new HashMap<>();

    private final List<String> conversations = new ArrayList<>();
    private boolean affichageConversations = false;

    // INITIALISER
    public void initialiser(String username, String role) {
        this.username = username;
        this.role     = role;

        membresEnLigne.clear();
        tousLesMembres.clear();
        notifications.clear();
        conversationsNonLues.clear();
        membresListView.getItems().clear();

        usernameLabel.setText(username);
        avatarLabel.setText(String.valueOf(username.charAt(0)).toUpperCase());
        roleLabel.setText(role);

        switch (role) {
            case "ORGANISATEUR" -> roleLabel.setStyle(
                    "-fx-background-color: rgba(255,200,0,0.2); -fx-text-fill: #FFD60A;" +
                            "-fx-background-radius: 20; -fx-padding: 3 10 3 10;" +
                            "-fx-font-size: 10px; -fx-font-weight: bold;");
            case "BENEVOLE" -> roleLabel.setStyle(
                    "-fx-background-color: rgba(50,215,75,0.2); -fx-text-fill: #32D74B;" +
                            "-fx-background-radius: 20; -fx-padding: 3 10 3 10;" +
                            "-fx-font-size: 10px; -fx-font-weight: bold;");
        }

        if (!role.equals("ORGANISATEUR")) {
            tabTous.setVisible(false);
            tabTous.setManaged(false);
        }

        demarrerEcoute();

        if (role.equals("ORGANISATEUR")) {
            affichageTous = true;
            //Activer visuellement l'onglet "Tous" par defaut
            tabTous.getStyleClass().add("tab-active");
            tabOnline.getStyleClass().remove("tab-active");
            ClientSocket.envoyer("MEMBERS");
            ClientSocket.envoyer("ONLINE_MEMBERS");
        } else {
            affichageTous = false;
            // Activer visuellement l'onglet "En ligne" par defaut
            tabOnline.getStyleClass().add("tab-active");
            ClientSocket.envoyer("ONLINE_MEMBERS");
            ClientSocket.envoyer("UNREAD_SENDERS");
        }

        ClientSocket.envoyer("CONVERSATIONS");
    }

    // ECOUTE SERVEUR
    private void demarrerEcoute() {
        ClientSocket.ecouterEnContinu(new ClientSocket.MessageListener() {
            @Override
            public void onMessage(String message) {
                Platform.runLater(() -> traiterMessage(message));
            }

            @Override
            public void onDeconnexion() {
                Platform.runLater(() -> {
                    afficherAlerte("Connexion perdue avec le serveur !");
                    retourLogin();
                });
            }
        });
    }

    // TRAITEMENT DES MESSAGES SERVEUR
    private void traiterMessage(String message) {

        // Liste membres en ligne
        if (message.startsWith("ONLINE_MEMBER ")) {
            String membre = message.split(" ")[1];
            if (!membre.equals(username) && !membresEnLigne.contains(membre)) {
                membresEnLigne.add(membre);
                mettreAJourListe();
            }
        }

        else if (message.equals("ONLINE_MEMBERS_END")) {
            onlineCountLabel.setText(String.valueOf(membresEnLigne.size()));
            mettreAJourListe();
        }

        // Liste tous les membres (ORGANISATEUR)
        else if (message.startsWith("MEMBER ")) {
            String[] parts = message.split(" ");
            String membre  = parts[1];
            if (!membre.equals(username) && !tousLesMembres.contains(membre)) {
                tousLesMembres.add(membre);
            }
        }

        else if (message.equals("MEMBERS_END")) {
            totalCountLabel.setText(String.valueOf(tousLesMembres.size()));
            mettreAJourListe();
        }

        //Expéditeurs avec messages non lus
        else if (message.startsWith("UNREAD_SENDER ")) {
            String expediteur = message.split(" ")[1];
            if (!conversationsNonLues.contains(expediteur)) {
                conversationsNonLues.add(expediteur);
            }
        }

        else if (message.equals("UNREAD_SENDERS_END")) {
            mettreAJourListe(); // reconstruire la liste avec les non lus
        }

        //Notifications temps reel
        else if (message.startsWith("USER_ONLINE ")) {
            String membre = message.split(" ")[1];
            if (!membre.equals(username) && !membresEnLigne.contains(membre)) {
                membresEnLigne.add(membre);
                onlineCountLabel.setText(String.valueOf(membresEnLigne.size()));
                mettreAJourListe();
                if (membre.equals(destinataireActif)) {
                    mettreAJourStatutChat(true);
                }
            }
        }

        else if (message.startsWith("USER_OFFLINE ")) {
            String membre = message.split(" ")[1];
            membresEnLigne.remove(membre);
            onlineCountLabel.setText(String.valueOf(membresEnLigne.size()));
            mettreAJourListe();
            if (membre.equals(destinataireActif)) {
                mettreAJourStatutChat(false);
            }
        }

        //Message reçu
        else if (message.startsWith("MSG ")) {
            String[] parts    = message.split(" ", 3);
            String expediteur = parts[1];
            String contenu    = parts[2];

            //Ajouter aux conversations
            if (!conversations.contains(expediteur)) {
                conversations.add(expediteur);
            }

            if (expediteur.equals(destinataireActif)) {
                // Chat ouvert = afficher la bulle directement
                ajouterBulle(contenu, false, LocalDateTime.now());
            } else {
                // Chat ferme = notification + ajouter aux non lus
                notifications.merge(expediteur, 1, Integer::sum);

                // Ajouter a la liste si pas encore visible
                if (!conversationsNonLues.contains(expediteur)) {
                    conversationsNonLues.add(expediteur);
                }
                mettreAJourListe();
            }
        }

        //Historique
        else if (message.startsWith("HISTORY_MSG ")) {
            String[] parts    = message.split(" ", 4);
            String expediteur = parts[1];
            String contenu    = parts[2];
            boolean estMoi    = expediteur.equals(username);
            ajouterBulle(contenu, estMoi, LocalDateTime.now());
        }

        else if (message.equals("HISTORY_END")) {
            // Marquer comme lu cote serveur
            ClientSocket.envoyer("READ " + destinataireActif);
            // Retirer des non lus cote client
            conversationsNonLues.remove(destinataireActif);
            mettreAJourListe();
            scrollerEnBas();
        }

        else if (message.equals("MSG_OK")) {
            scrollerEnBas();
        }

        //conversation avec du contenu
        else if (message.startsWith("CONVERSATION ")) {
            String interlocuteur = message.split(" ")[1];
            if (!conversations.contains(interlocuteur)
                    && !interlocuteur.equals(username)) {
                conversations.add(interlocuteur);
            }
        }

        else if (message.equals("CONVERSATIONS_END")) {
            // Pas besoin de mettreAJourListe() ici
            // seulement si on est sur l'onglet conversations
            if (affichageConversations) mettreAJourListe();
        }
    }

    // OUVRIR CHAT
    public void ouvrirChat(String destinataire) {
        this.destinataireActif = destinataire;

        notifications.remove(destinataire);
        mettreAJourListe();

        chatDestinataireLabel.setText(destinataire);
        chatAvatarLabel.setText(String.valueOf(destinataire.charAt(0)).toUpperCase());
        mettreAJourStatutChat(membresEnLigne.contains(destinataire));

        messagesContainer.getChildren().clear();

        welcomePane.setVisible(false);
        welcomePane.setManaged(false);
        chatPane.setVisible(true);
        chatPane.setManaged(true);

        messageField.requestFocus();
        ClientSocket.envoyer("HISTORY " + destinataire);


        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > oldVal.doubleValue()) {
                scrollerEnBas();
            }
        });
    }


    // ENVOYER MESSAGE
    @FXML
    public void envoyerMessage() {
        if (destinataireActif == null) return;

        String contenu = messageField.getText().trim();
        if (contenu.isEmpty()) return;

        ClientSocket.envoyer("MSG " + destinataireActif + " " + contenu);
        ajouterBulle(contenu, true, LocalDateTime.now());
        messageField.clear();

        //ajouter a la conversation
        if (!conversations.contains(destinataireActif)) {
            conversations.add(destinataireActif);
        }
    }

    // BULLE DE MESSAGE
    private void ajouterBulle(String contenu, boolean estMoi, LocalDateTime date) {
        VBox bulle = new VBox(4);
        Label texte = new Label(contenu);
        texte.setWrapText(true);
        texte.setMaxWidth(320);
        Label heure = new Label(date.format(DateTimeFormatter.ofPattern("HH:mm")));

        if (estMoi) {
            texte.getStyleClass().add("bubble-text-sent");
            heure.getStyleClass().add("bubble-time");
            bulle.getStyleClass().add("bubble-sent");
            bulle.setAlignment(Pos.CENTER_RIGHT);
            bulle.getChildren().addAll(texte, heure);
            HBox wrapper = new HBox(bulle);
            wrapper.setAlignment(Pos.CENTER_RIGHT);
            wrapper.setPadding(new Insets(4, 10, 4, 50));
            messagesContainer.getChildren().add(wrapper);
        } else {
            texte.getStyleClass().add("bubble-text-received");
            heure.getStyleClass().add("bubble-time");
            bulle.getStyleClass().add("bubble-received");
            bulle.setAlignment(Pos.CENTER_LEFT);
            bulle.getChildren().addAll(texte, heure);
            HBox wrapper = new HBox(bulle);
            wrapper.setAlignment(Pos.CENTER_LEFT);
            wrapper.setPadding(new Insets(4, 50, 4, 10));
            messagesContainer.getChildren().add(wrapper);
        }

        scrollerEnBas();
    }

    // MISE A JOUR LISTE — en ligne + non lus
    private void mettreAJourListe() {
        List<String> aAfficher = new ArrayList<>();

        if (affichageConversations) {
            // Onglet Conversations
            aAfficher.addAll(conversations);

        } else if (role != null && role.equals("ORGANISATEUR")) {
            if (affichageTous) {
                aAfficher.addAll(tousLesMembres);
            } else {
                aAfficher.addAll(membresEnLigne);
            }
            for (String exp : conversationsNonLues) {
                if (!aAfficher.contains(exp) && !exp.equals(username)) {
                    aAfficher.add(exp);
                }
            }
        } else {
            aAfficher.addAll(membresEnLigne);
            for (String exp : conversationsNonLues) {
                if (!aAfficher.contains(exp) && !exp.equals(username)) {
                    aAfficher.add(exp);
                }
            }
        }

        // Trier en ligne d'abord, non lus ensuite
        aAfficher.sort((a, b) -> {
            boolean aEnLigne = membresEnLigne.contains(a);
            boolean bEnLigne = membresEnLigne.contains(b);
            boolean aNonLu   = conversationsNonLues.contains(a);
            boolean bNonLu   = conversationsNonLues.contains(b);

            if (aEnLigne && !bEnLigne) return -1;
            if (!aEnLigne && bEnLigne) return 1;
            if (aNonLu && !bNonLu)    return -1;
            if (!aNonLu && bNonLu)    return 1;
            return a.compareTo(b);
        });

        String recherche = searchField != null ? searchField.getText().toLowerCase() : "";
        List<HBox> cellules = aAfficher.stream()
                .filter(m -> m.toLowerCase().contains(recherche))
                .map(this::creerCelluleMembre)
                .toList();

        membresListView.setItems(FXCollections.observableArrayList(cellules));
    }

    // CELLULE MEMBRE
    private HBox creerCelluleMembre(String membre) {
        HBox cell = new HBox(10);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(8, 10, 8, 10));

        // Avatar
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("avatar-circle");
        avatar.setMinWidth(36); avatar.setMaxWidth(36);
        avatar.setMinHeight(36); avatar.setMaxHeight(36);
        Label avatarTxt = new Label(String.valueOf(membre.charAt(0)).toUpperCase());
        avatarTxt.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        avatar.getChildren().add(avatarTxt);

        // Nom + statut
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nom = new Label(membre);
        nom.getStyleClass().add("membre-name");

        boolean enLigne = membresEnLigne.contains(membre);
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        StackPane dot = new StackPane();
        dot.setMinWidth(7); dot.setMaxWidth(7);
        dot.setMinHeight(7); dot.setMaxHeight(7);
        dot.setStyle("-fx-background-radius: 50; -fx-background-color: " +
                (enLigne ? "#32D74B" : "#636366") + ";");
        Label statusTxt = new Label(enLigne ? "En ligne" : "Hors ligne");
        statusTxt.setStyle("-fx-font-size: 10px; -fx-text-fill: " +
                (enLigne ? "#32D74B" : "#636366") + ";");
        statusBox.getChildren().addAll(dot, statusTxt);
        info.getChildren().addAll(nom, statusBox);

        // Badge notification
        int notifs = notifications.getOrDefault(membre, 0);
        StackPane badge = new StackPane();
        if (notifs > 0) {
            badge.setMinWidth(18); badge.setMaxWidth(18);
            badge.setMinHeight(18); badge.setMaxHeight(18);
            badge.setStyle("-fx-background-color: #FF3B30; -fx-background-radius: 50;");
            Label badgeTxt = new Label(String.valueOf(notifs));
            badgeTxt.setStyle(
                    "-fx-text-fill: white; -fx-font-size: 9px; -fx-font-weight: bold;");
            badge.getChildren().add(badgeTxt);
        }

        // Bouton chat
        Button btnChat = new Button("💬");
        btnChat.getStyleClass().add("btn-chat");

        // Highlight si destinataire actif
        if (membre.equals(destinataireActif)) {
            cell.setStyle(
                    "-fx-background-color: rgba(45,107,228,0.2); -fx-background-radius: 10;");
        }

        btnChat.setOnAction(e -> ouvrirChat(membre));
        cell.setOnMouseClicked(e -> ouvrirChat(membre));

        cell.getChildren().addAll(avatar, info, badge, btnChat);
        return cell;
    }

    // STATUT CHAT HEADER
    private void mettreAJourStatutChat(boolean enLigne) {
        chatStatusLabel.setText(enLigne ? "En ligne" : "Hors ligne");
        chatStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " +
                (enLigne ? "#32D74B" : "#636366") + ";");
        chatStatusDot.setStyle(
                "-fx-background-radius: 50; -fx-background-color: " +
                        (enLigne ? "#32D74B" : "#636366") +
                        "; -fx-min-width: 8; -fx-min-height: 8;" +
                        "-fx-max-width: 8; -fx-max-height: 8;");
    }

    private void scrollerEnBas() {
        Platform.runLater(() -> {
            if (scrollPane != null && messagesContainer != null) {
                // Force le calcul de la hauteur
                messagesContainer.applyCss();
                messagesContainer.layout();

                // Scrolle tout en bas
                scrollPane.setVvalue(1.0);

                // Optionnel: animation douce
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(scrollPane.vvalueProperty(), 1.0))
                );
                timeline.play();
            }
        });
    }


    // ONGLETS
    @FXML public void afficherConversations() {
        affichageConversations = true;
        affichageTous = false;
        tabConversations.getStyleClass().add("tab-active");
        tabOnline.getStyleClass().remove("tab-active");
        tabTous.getStyleClass().remove("tab-active");

        // Rafraichir depuis le serveur à chaque fois
        conversations.clear();
        ClientSocket.envoyer("CONVERSATIONS");
    }

    @FXML public void afficherEnLigne() {
        affichageConversations = false; //desactiver conversations
        affichageTous = false;
        tabOnline.getStyleClass().add("tab-active");
        tabTous.getStyleClass().remove("tab-active");
        tabConversations.getStyleClass().remove("tab-active");
        mettreAJourListe();
    }

    @FXML public void afficherTous() {
        affichageConversations = false; //desactiver conversations
        affichageTous = true;
        tabTous.getStyleClass().add("tab-active");
        tabOnline.getStyleClass().remove("tab-active");
        tabConversations.getStyleClass().remove("tab-active");
        if (tousLesMembres.isEmpty()) {
            ClientSocket.envoyer("MEMBERS");
        } else {
            mettreAJourListe();
        }
    }

    @FXML public void filtrerMembres() { mettreAJourListe(); }

    // DECONNEXION
    @FXML
    public void seDeconnecter() {
        ClientSocket.deconnecter();
        retourLogin();
    }

    @FXML
    public void fermerChat() {
        destinataireActif = null;
        welcomePane.setVisible(true);
        welcomePane.setManaged(true);
        chatPane.setVisible(false);
        chatPane.setManaged(false);
    }

    private void retourLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/ghost/chat_examen/login-view.fxml"));
            stage.setScene(new Scene(loader.load(), 900, 520));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afficherAlerte(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}