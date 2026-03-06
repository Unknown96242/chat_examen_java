
package org.ghost.chat_examen.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Database {
    private static EntityManagerFactory emf;

    // Initialisation une seule fois
    static {
        emf = Persistence.createEntityManagerFactory("PERSISTENCE");
    }

    // Obtenir un EntityManager
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // Fermeture (à la fin de l'application)
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}