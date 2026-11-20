package fr.diginamic.hello.aventures.dao;

import fr.diginamic.hello.aventures.modeles.Aventure;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AventureDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Returns all adventures and their steps.
     */
    public List<Aventure> findAll() {
        return entityManager.createQuery(
                "SELECT DISTINCT a FROM Aventure a " +
                        "LEFT JOIN FETCH a.etapes",
                Aventure.class
        ).getResultList();
    }

    /**
     * Finds an adventure by its identifier.
     */
    public Aventure findById(int id) {
        return entityManager.createQuery(
                        "SELECT DISTINCT a FROM Aventure a " +
                                "LEFT JOIN FETCH a.etapes WHERE a.id = :id",
                        Aventure.class
                ).setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds adventures whose name starts with a prefix.
     */
    public List<Aventure> findByNom(String prefixe) {
        return entityManager.createQuery(
                "SELECT DISTINCT a FROM Aventure a " +
                        "LEFT JOIN FETCH a.etapes " +
                        "WHERE LOWER(a.nom) LIKE :prefixe",
                Aventure.class
        ).setParameter(
                "prefixe", prefixe.toLowerCase() + "%"
        ).getResultList();
    }

    /**
     * Finds adventures above a difficulty threshold.
     */
    public List<Aventure> findByDifficulteMin(int min) {
        return entityManager.createQuery(
                        "SELECT DISTINCT a FROM Aventure a " +
                                "LEFT JOIN FETCH a.etapes " +
                                "WHERE a.difficulte > :min",
                        Aventure.class
                ).setParameter("min", min)
                .getResultList();
    }

    /**
     * Finds adventures between two difficulty values.
     */
    public List<Aventure> findByDifficulteIntervalle(
            int min, int max) {

        return entityManager.createQuery(
                        "SELECT DISTINCT a FROM Aventure a " +
                                "LEFT JOIN FETCH a.etapes " +
                                "WHERE a.difficulte > :min " +
                                "AND a.difficulte < :max",
                        Aventure.class
                ).setParameter("min", min)
                .setParameter("max", max)
                .getResultList();
    }

    /**
     * Persists a new adventure.
     */
    public void save(Aventure aventure) {
        entityManager.persist(aventure);
    }

    /**
     * Removes an existing adventure.
     */
    public void delete(Aventure aventure) {
        entityManager.remove(aventure);
    }
}
