package org.ghost.chat_examen.server;

import org.ghost.chat_examen.dao.MessageDao;
import org.ghost.chat_examen.dao.UserDao;
import org.ghost.chat_examen.entities.Message;
import org.ghost.chat_examen.entities.User;
import org.ghost.chat_examen.enums.MessageStatut;
import org.ghost.chat_examen.enums.Role;
import org.ghost.chat_examen.enums.Status;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    private final UserDao userDAO = new UserDao();
    private final MessageDao messageDAO = new MessageDao();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String ligne;
            while ((ligne = in.readLine()) != null) {
                System.out.println("[SERVEUR] Reçu : " + ligne);

                if      (ligne.startsWith("REGISTER")) gererRegister(ligne);
                else if (ligne.startsWith("LOGIN"))    gererLogin(ligne);
                else if (ligne.startsWith("MSG"))      gererMessage(ligne);
                else if (ligne.startsWith("HISTORY"))  gererHistory(ligne);
                else if (ligne.equals("MEMBERS"))      gererMembers();
                else if (ligne.equals("UNREAD_SENDERS")) gererUnreadSenders();
                else if (ligne.equals("ONLINE_MEMBERS"))     gererOnlineMembers();
                else if (ligne.equals("CONVERSATIONS")) gererConversations();
                else if (ligne.startsWith("READ")) gererRead(ligne);
                else if (ligne.equals("LOGOUT"))     { gererLogout(); break; }
            }

        } catch (IOException e) {
            System.out.println("[SERVEUR] Connexion perdue avec : " + username);
        } finally {
            deconnecter();
        }
    }

    //  REGISTER
    private void gererRegister(String ligne) {
        // "REGISTER alice monmdp MEMBRE"
        String[] parts = ligne.split(" ", 4);
        if (parts.length < 4) {
            out.println("REGISTER_FAIL Format invalide");
            return;
        }

        String user = parts[1];
        String mdp  = parts[2];
        String role = parts[3];

        if (userDAO.findByUsername(user) != null) {
            out.println("REGISTER_FAIL Username déjà pris");
            return;
        }

        String mdpHache = BCrypt.hashpw(mdp, BCrypt.gensalt());

        User newUser = new User();
        newUser.setUsername(user);
        newUser.setPassword(mdpHache);
        newUser.setRole(Role.valueOf(role));
        // status et dateCreation gérés par @PrePersist

        userDAO.save(newUser);
        out.println("REGISTER_OK");
        System.out.println("[SERVEUR] Nouvel utilisateur inscrit : " + user);
    }


    //  LOGIN
    private void gererLogin(String ligne) {
        // "LOGIN alice monmdp"
        String[] parts = ligne.split(" ", 3);
        if (parts.length < 3) {
            out.println("LOGIN_FAIL Format invalide");
            return;
        }

        String user = parts[1];
        String mdp  = parts[2];

        User u = userDAO.findByUsername(user);

        if (u == null || !BCrypt.checkpw(mdp, u.getPassword())) {
            out.println("LOGIN_FAIL Identifiants incorrects");
            return;
        }

        if (Server.clientsConnectes.containsKey(user)) {
            out.println("LOGIN_FAIL Déjà connecté ailleurs");
            return;
        }

        this.username = user;
        Server.clientsConnectes.put(username, out);

        userDAO.updateStatus(username, Status.ONLINE);

        out.println("LOGIN_OK " + u.getRole());
        System.out.println("[SERVEUR] " + username + " connecté");

        Server.broadcast("USER_ONLINE " + username, username);

        //livrer les messages en attente
        livrerMessagesEnAttente();
    }

    //  MSG
    private void gererMessage(String ligne) {
        // "MSG bob Salut comment tu vas ?"
        if (username == null) {
            out.println("ERROR Non authentifié");
            return;
        }

        String[] parts = ligne.split(" ", 3);
        if (parts.length < 3) {
            out.println("ERROR Format invalide");
            return;
        }

        String destinataire = parts[1];
        String contenu      = parts[2];

        if (contenu.isBlank()) {
            out.println("ERROR Message invalide (vide)");
            return;
        }


        User dest = userDAO.findByUsername(destinataire);
        if (dest == null) {
            out.println("ERROR Destinataire introuvable");
            return;
        }

        Message msg = new Message();
        msg.setSender(userDAO.findByUsername(this.username));
        msg.setReceiver(dest);
        msg.setContenu(contenu);
        messageDAO.save(msg);

        System.out.println("[SERVEUR] Message de " + username + " → " + destinataire);

        // Destinataire en ligne ?
        PrintWriter destWriter = Server.clientsConnectes.get(destinataire);
        if (destWriter != null) {
            destWriter.println("MSG " + this.username + " " + contenu);
            msg.setStatut(MessageStatut.RECU);
            messageDAO.update(msg);
        }
        // Sinon reste ENVOYE en BDD, livré à la reconnexion

        out.println("MSG_OK");
    }

    //  HISTORY
    private void gererHistory(String ligne) {
        // "HISTORY bob"
        if (username == null) {
            out.println("ERROR Non authentifié");
            return;
        }

        String[] parts = ligne.split(" ");
        if (parts.length < 2) {
            out.println("ERROR Format invalide");
            return;
        }

        String autreUser = parts[1];

        List<Message> historique = messageDAO.getHistorique(this.username, autreUser);

        for (Message m : historique) {
            out.println("HISTORY_MSG "
                    + m.getSender().getUsername() + " "
                    + m.getContenu() + " "
                    + m.getDateEnvoi()
            );
        }

        out.println("HISTORY_END");
    }

    //  MEMBERS
    private void gererMembers() {
        if (username == null) {
            out.println("ERROR Non authentifié");
            return;
        }

        User u = userDAO.findByUsername(username);

        // seulement ORGANISATEUR
        if (u.getRole() != Role.ORGANISATEUR) {
            out.println("ERROR Accès refusé");
            return;
        }

        List<User> tousLesMembers = userDAO.findAll();
        for (User membre : tousLesMembers) {
            out.println("MEMBER " + membre.getUsername() + " " + membre.getStatus());
        }
        out.println("MEMBERS_END");
    }

    // Envoyer la liste des membres actuellement connectés
    private void gererOnlineMembers() {
        if (username == null) {
            out.println("ERROR Non authentifié");
            return;
        }

        Server.clientsConnectes.forEach((user, writer) -> {
            if (!user.equals(this.username)) {
                out.println("ONLINE_MEMBER " + user);
            }
        });
        out.println("ONLINE_MEMBERS_END");
    }


    //  LOGOUT
    private void gererLogout() {
        System.out.println("[SERVEUR] " + username + " déconnecté");
        deconnecter();
    }

    private void gererUnreadSenders() {
        if (username == null) {
            out.println("ERROR Non authentifié");
            return;
        }

        // Récupérer les expéditeurs distincts ayant des messages non lus
        List<String> expediteurs = messageDAO.getExpediteursNonLus(this.username);
        for (String exp : expediteurs) {
            out.println("UNREAD_SENDER " + exp);
        }
        out.println("UNREAD_SENDERS_END");
    }


    //  MESSAGES EN ATTENTE
    private void livrerMessagesEnAttente() {
        List<Message> enAttente = messageDAO.getMessagesNonLivres(this.username);

        for (Message m : enAttente) {
            // Vérifier que sender et contenu existent bien
            if (m.getSender() == null || m.getContenu() == null) continue;

            String expediteur = m.getSender().getUsername();
            String contenu    = m.getContenu();
            String dateEnvoi  = m.getDateEnvoi().toString();

            // Envoyer avec la date pour que le client sache quand c'était
            out.println("MSG " + expediteur + " " + contenu);

            // Mettre à jour le statut
            m.setStatut(MessageStatut.RECU);
            messageDAO.update(m);

            System.out.println("[SERVEUR] Message en attente livré : "
                    + expediteur + " → " + this.username);
        }

        if (!enAttente.isEmpty()) {
            System.out.println("[SERVEUR] "
                    + enAttente.size() + " messages livrés à " + this.username);
        }
    }

    private void gererConversations() {
        if (username == null) {
            out.println("ERROR Non authentifié");
            return;
        }

        List<String> interlocuteurs = messageDAO.getInterlocuteurs(this.username);
        for (String interlocuteur : interlocuteurs) {
            out.println("CONVERSATION " + interlocuteur);
        }
        out.println("CONVERSATIONS_END");
    }

    private void gererRead(String ligne) {
        // "READ expediteur"
        if (username == null) return;

        String[] parts = ligne.split(" ");
        if (parts.length < 2) return;

        String expediteur = parts[1];

        // Marquer tous les messages de cet expediteur comme LU
        messageDAO.marquerCommeLu(expediteur, this.username);
        System.out.println("[SERVEUR] Messages de " + expediteur
                + " marqués LU pour " + username);
    }

    //  DECONNEXION — appelé dans tous les cas (finally)
    private void deconnecter() {
        if (username != null) {
            Server.clientsConnectes.remove(username);
            userDAO.updateStatus(username, Status.OFFLINE);
            Server.broadcast("USER_OFFLINE " + username, username);
        }
        try { socket.close(); } catch (IOException ignored) {}
    }
}