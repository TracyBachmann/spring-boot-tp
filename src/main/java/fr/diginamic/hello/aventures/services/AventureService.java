package fr.diginamic.hello.aventures.services;

import fr.diginamic.hello.aventures.dao.AventureDao;
import fr.diginamic.hello.aventures.modeles.Aventure;
import fr.diginamic.hello.aventures.modeles.Etape;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AventureService {

    private final AventureDao aventureDao;

    public AventureService(AventureDao aventureDao) {
        this.aventureDao = aventureDao;
    }

    /**
     * Returns all adventures.
     */
    @Transactional(readOnly = true)
    public List<Aventure> extractAventures() {
        return aventureDao.findAll();
    }

    /**
     * Returns an adventure by its identifier.
     */
    @Transactional(readOnly = true)
    public Aventure extractAventure(int idAventure) {
        Aventure aventure = aventureDao.findById(idAventure);

        if (aventure == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Aventure introuvable"
            );
        }

        return aventure;
    }

    /**
     * Returns adventures whose name starts with a prefix.
     */
    @Transactional(readOnly = true)
    public List<Aventure> extractAventures(String prefixe) {
        return aventureDao.findByNom(prefixe);
    }

    /**
     * Returns adventures above a difficulty threshold.
     */
    @Transactional(readOnly = true)
    public List<Aventure> extractAventures(int min) {
        return aventureDao.findByDifficulteMin(min);
    }

    /**
     * Returns adventures in a difficulty interval.
     */
    @Transactional(readOnly = true)
    public List<Aventure> extractAventures(int min, int max) {
        if (min >= max) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le minimum doit être inférieur au maximum"
            );
        }

        return aventureDao.findByDifficulteIntervalle(min, max);
    }

    /**
     * Inserts an adventure with its steps.
     */
    @Transactional
    public List<Aventure> insertAventure(Aventure nouvelle) {
        verifierAventure(nouvelle);

        Aventure aventure = new Aventure(
                nouvelle.getNom().trim(),
                nouvelle.getDifficulte()
        );

        if (nouvelle.getEtapes() != null) {
            for (Etape etape : nouvelle.getEtapes()) {
                aventure.ajouterEtape(copierEtape(etape));
            }
        }

        aventureDao.save(aventure);
        return aventureDao.findAll();
    }

    /**
     * Updates an adventure and replaces its steps.
     */
    @Transactional
    public List<Aventure> modifierAventure(
            int idAventure, Aventure modification) {

        verifierAventure(modification);

        Aventure aventure = aventureDao.findById(idAventure);

        if (aventure == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Aventure introuvable"
            );
        }

        aventure.setNom(modification.getNom().trim());
        aventure.setDifficulte(modification.getDifficulte());

        // Replace the old steps with the submitted steps.
        aventure.getEtapes().clear();

        if (modification.getEtapes() != null) {
            for (Etape etape : modification.getEtapes()) {
                aventure.ajouterEtape(copierEtape(etape));
            }
        }

        return aventureDao.findAll();
    }

    /**
     * Deletes an adventure by its identifier.
     */
    @Transactional
    public List<Aventure> supprimerAventure(int idAventure) {
        Aventure aventure = aventureDao.findById(idAventure);

        if (aventure == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Aventure introuvable"
            );
        }

        aventureDao.delete(aventure);

        return aventureDao.findAll();
    }

    private void verifierAventure(Aventure aventure) {
        if (aventure == null
                || aventure.getNom() == null
                || aventure.getNom().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le nom de l'aventure est obligatoire"
            );
        }

        if (aventure.getDifficulte() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La difficulté ne peut pas être négative"
            );
        }
    }

    private Etape copierEtape(Etape source) {
        Etape copie = new Etape();
        copie.setCode(source.getCode());
        copie.setDateDebut(source.getDateDebut());
        copie.setDateFin(source.getDateFin());
        return copie;
    }
}
