package org.ghost.chat_examen.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    public static Map<String, PrintWriter> clientsConnectes = new ConcurrentHashMap<>();
    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║   Serveur Messagerie démarré...  ║");
        System.out.println("║   Port : " + PORT + "                    ║");
        System.out.println("╚══════════════════════════════════╝");

        ExecutorService pool = Executors.newFixedThreadPool(50);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVEUR] Nouveau client connecté depuis : "
                        + clientSocket.getInetAddress().getHostAddress());
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("[SERVEUR] Erreur fatale : " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    public static void broadcast(String message, String excludeUsername) {
        clientsConnectes.forEach((username, writer) -> {
            if (!username.equals(excludeUsername)) {
                writer.println(message);
            }
        });
    }

    public static boolean estEnLigne(String username) {
        return clientsConnectes.containsKey(username);
    }
}