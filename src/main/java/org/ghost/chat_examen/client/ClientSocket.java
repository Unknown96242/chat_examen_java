package org.ghost.chat_examen.client;

import java.io.*;
import java.net.Socket;

public class ClientSocket {

        private static Socket socket;
        private static PrintWriter out;
        private static BufferedReader in;
        private static boolean deconnexionVolontaire = false;

        // Connexion au serveur — appelé depuis LoginController
        public static void connecter() throws Exception {
            deconnexionVolontaire = false;
            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        // Envoyer une commande au serveur
        // ex: ClientSocket.envoyer("LOGIN alice monmdp")
        public static void envoyer(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        // Lire UNE réponse du serveur
        // Utilisé pour attendre LOGIN_OK, REGISTER_OK
        public static String lire() throws Exception {
            return in.readLine();
        }

        // Écouter en continu les messages entrants
        // Appelé après le login — tourne dans un thread séparé
        public static void ecouterEnContinu(MessageListener listener) {
            new Thread(() -> {
                try {
                    String ligne;
                    while ((ligne = in.readLine()) != null) {
                        final String msg = ligne;
                        listener.onMessage(msg); // on délègue au controller
                    }
                } catch (IOException e) {
                    if (!deconnexionVolontaire) {
                        listener.onDeconnexion();
                    } // perte de connexion
                }
            }).start();
        }

        // Déconnexion
        public static void deconnecter() {
            deconnexionVolontaire = true;
            envoyer("LOGOUT");
            try {
                if (socket != null) socket.close();
            } catch (IOException ignored) {}
        }

        public static boolean estConnecte() {
            return socket != null && socket.isConnected() && !socket.isClosed();
        }

        // Interface — le controller implémente ça pour recevoir
        // les messages en temps réel
        public interface MessageListener {
            void onMessage(String message);   // appelé à chaque ligne reçue
            void onDeconnexion();             // appelé si connexion perdue
        }
    }