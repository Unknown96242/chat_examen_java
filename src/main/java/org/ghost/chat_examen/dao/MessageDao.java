package org.ghost.chat_examen.dao;

import org.ghost.chat_examen.entities.Message;
import org.ghost.chat_examen.enums.MessageStatut;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class MessageDao {

    // Sauvegarder un message en BDD
    public void save(Message message) {
        EntityManager em = Database.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(message);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // Mettre à jour le statut d'un message
    // ENVOYE → RECU → LU
    public void update(Message message) {
        EntityManager em = Database.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(message);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }


    // Historique entre deux users
    public List<Message> getHistorique(String username1, String username2) {
        EntityManager em = Database.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM Message m " +
                                    "WHERE (m.sender.username = :u1 AND m.receiver.username = :u2) " +
                                    "   OR (m.sender.username = :u2 AND m.receiver.username = :u1) " +
                                    "ORDER BY m.dateEnvoi ASC", Message.class) // RG8
                    .setParameter("u1", username1)
                    .setParameter("u2", username2)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Messages non livrés pour un user
    // Appelé à la reconnexion
    public List<Message> getMessagesNonLivres(String username) {
        EntityManager em = Database.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM Message m " +
                                    "WHERE m.receiver.username = :username " +
                                    "AND m.statut = :statut " +
                                    "ORDER BY m.dateEnvoi ASC", Message.class)
                    .setParameter("username", username)
                    .setParameter("statut", MessageStatut.ENVOYE)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<String> getExpediteursNonLus(String username) {
        EntityManager em = Database.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT m.sender.username FROM Message m " +
                                    "WHERE m.receiver.username = :username " +
                                    "AND m.statut = :statut " +
                                    "ORDER BY m.sender.username ASC", String.class)
                    .setParameter("username", username)
                    .setParameter("statut", MessageStatut.RECU)
                    .getResultList();
        } finally {
            em.close();
        }
    }


    public void marquerCommeLu(String expediteur, String destinataire) {
        EntityManager em = Database.getEntityManager();
        try {
            em.getTransaction().begin();

            // Récupérer les messages concernés puis les modifier un par un
            List<Message> messages = em.createQuery(
                            "SELECT m FROM Message m " +
                                    "WHERE m.sender.username = :expediteur " +
                                    "AND m.receiver.username = :destinataire " +
                                    "AND m.statut = :statut", Message.class)
                    .setParameter("expediteur", expediteur)
                    .setParameter("destinataire", destinataire)
                    .setParameter("statut", MessageStatut.RECU)
                    .getResultList();

            for (Message m : messages) {
                m.setStatut(MessageStatut.LU);
                em.merge(m);
            }

            em.getTransaction().commit();
            System.out.println("[DAO] " + messages.size()
                    + " message(s) marqués LU de "
                    + expediteur + " pour " + destinataire);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }


    public List<String> getInterlocuteurs(String username) {
        EntityManager em = Database.getEntityManager();
        try {
            //Tous les gens avec qui on a echange (envoye OU reçu)
            List<String> envoyes = em.createQuery(
                            "SELECT DISTINCT m.receiver.username FROM Message m " +
                                    "WHERE m.sender.username = :username", String.class)
                    .setParameter("username", username)
                    .getResultList();

            List<String> recus = em.createQuery(
                            "SELECT DISTINCT m.sender.username FROM Message m " +
                                    "WHERE m.receiver.username = :username", String.class)
                    .setParameter("username", username)
                    .getResultList();

            // Fusionner sans doublons
            List<String> tous = new ArrayList<>(envoyes);
            for (String s : recus) {
                if (!tous.contains(s)) tous.add(s);
            }
            return tous;

        } finally {
            em.close();
        }
    }
}