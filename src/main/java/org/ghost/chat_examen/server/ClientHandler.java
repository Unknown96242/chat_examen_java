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

    }


    //  LOGIN
    private void gererLogin(String ligne) {

    }

    //  MSG
    private void gererMessage(String ligne) {

    }

    //  HISTORY
    private void gererHistory(String ligne) {

    }

    //  MEMBERS
    private void gererMembers() {

    }

    // Envoyer la liste des membres actuellement connectés
    private void gererOnlineMembers() {

    }


    //  LOGOUT
    private void gererLogout() {
        System.out.println("[SERVEUR] " + username + " déconnecté");
        deconnecter();
    }

    private void gererUnreadSenders() {

    }


    //  MESSAGES EN ATTENTE
    private void livrerMessagesEnAttente() {

    }

    private void gererConversations() {

    }

    private void gererRead(String ligne) {

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