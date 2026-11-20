package fr.diginamic.hello.aventures.controleurs;

import fr.diginamic.hello.aventures.modeles.Aventure;
import fr.diginamic.hello.aventures.services.AventureService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aventures")
public class AventureControleur {

    private final AventureService aventureService;

    public AventureControleur(AventureService aventureService) {
        this.aventureService = aventureService;
    }

    /**
     * Returns all adventures.
     */
    @GetMapping
    public List<Aventure> getAventures() {
        return aventureService.extractAventures();
    }

    /**
     * Returns an adventure by its ID.
     */
    @GetMapping("/{id}")
    public Aventure getAventure(@PathVariable int id) {
        return aventureService.extractAventure(id);
    }

    /**
     * Searches adventures by name prefix.
     */
    @GetMapping("/recherche/nom")
    public List<Aventure> getParNom(
            @RequestParam String prefixe) {

        return aventureService.extractAventures(prefixe);
    }

    /**
     * Searches adventures above a difficulty threshold.
     */
    @GetMapping("/recherche/difficulte")
    public List<Aventure> getParDifficulte(
            @RequestParam int min) {

        return aventureService.extractAventures(min);
    }

    /**
     * Searches adventures within a difficulty interval.
     */
    @GetMapping("/recherche/intervalle")
    public List<Aventure> getParIntervalle(
            @RequestParam int min,
            @RequestParam int max) {

        return aventureService.extractAventures(min, max);
    }

    /**
     * Creates an adventure.
     */
    @PostMapping
    public List<Aventure> ajouterAventure(
            @RequestBody Aventure aventure) {

        return aventureService.insertAventure(aventure);
    }

    /**
     * Updates an adventure.
     */
    @PutMapping("/{id}")
    public List<Aventure> modifierAventure(
            @PathVariable int id,
            @RequestBody Aventure aventure) {

        return aventureService.modifierAventure(id, aventure);
    }

    /**
     * Deletes an adventure.
     */
    @DeleteMapping("/{id}")
    public List<Aventure> supprimerAventure(
            @PathVariable int id) {

        return aventureService.supprimerAventure(id);
    }
}
