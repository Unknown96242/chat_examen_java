package org.ghost.chat_examen.dao;

import org.ghost.chat_examen.entities.User;
import org.ghost.chat_examen.enums.Status;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

public class UserDao {

    // Créer un utilisateur
    public void save(User user) {
        EntityManager em = Database.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // Trouver par username
    public User findByUsername(String username) {
        EntityManager em = Database.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public User findById(Long id) {
        EntityManager em = Database.getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    // Liste tous les membres
    public List<User> findAll() {
        EntityManager em = Database.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u", User.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Mettre à jour le statut ONLINE/OFFLINE
    public void updateStatus(String username, Status status) {
        EntityManager em = Database.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery(
                            "UPDATE User u SET u.status = :status WHERE u.username = :username")
                    .setParameter("status", status)
                    .setParameter("username", username)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // Liste des membres en ligne
    public List<User> findOnlineUsers() {
        EntityManager em = Database.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.status = :status", User.class)
                    .setParameter("status", Status.ONLINE)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}