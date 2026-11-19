package fr.diginamic.hello.controleurs;

import fr.diginamic.hello.exceptions.VilleException;
import fr.diginamic.hello.modeles.Ville;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/villes")
public class VilleControleur {

    private final List<Ville> villes = new ArrayList<>();
    private int prochainId = 1;

    @PostConstruct
    public void initData() {
        villes.add(new Ville(prochainId++, "Nice", 342000));
        villes.add(new Ville(prochainId++, "Carcassonne", 47800));
        villes.add(new Ville(prochainId++, "Narbonne", 53400));
        villes.add(new Ville(prochainId++, "Lyon", 496000));
    }

    // Retrieve all cities
    @GetMapping
    public List<Ville> getVilles() {
        return villes;
    }

    // Retrieve a city by its ID
    @GetMapping("/{id}")
    public ResponseEntity<Ville> getVille(@PathVariable int id) {
        for (Ville ville : villes) {
            if (ville.getId() == id) {
                return ResponseEntity.ok(ville);
            }
        }

        return ResponseEntity.notFound().build();
    }

    // Add a city after validating its fields
    @PostMapping
    public ResponseEntity<String> ajouterVille(
            @Valid @RequestBody Ville ville) throws VilleException {

        String nom = ville.getNom().trim();

        for (Ville villeExistante : villes) {
            if (villeExistante.getNom().equalsIgnoreCase(nom)) {
                throw new VilleException("La ville existe déjà");
            }
        }

        ville.setNom(nom);
        ville.setId(prochainId++);
        villes.add(ville);

        return ResponseEntity.ok("Ville insérée avec succès");
    }

    // Update a city after validating the request body
    @PutMapping("/{id}")
    public ResponseEntity<String> modifierVille(
            @PathVariable int id,
            @Valid @RequestBody Ville villeModifiee)
            throws VilleException {

        Ville villeExistante = null;

        for (Ville ville : villes) {
            if (ville.getId() == id) {
                villeExistante = ville;
                break;
            }
        }

        if (villeExistante == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Ville introuvable");
        }

        String nouveauNom = villeModifiee.getNom().trim();

        for (Ville ville : villes) {
            if (ville.getId() != id
                    && ville.getNom().equalsIgnoreCase(nouveauNom)) {
                throw new VilleException("La ville existe déjà");
            }
        }

        villeExistante.setNom(nouveauNom);
        villeExistante.setPopulation(villeModifiee.getPopulation());

        return ResponseEntity.ok("Ville modifiée avec succès");
    }

    // Delete a city by its ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerVille(@PathVariable int id) {

        boolean supprimee = villes.removeIf(
                ville -> ville.getId() == id
        );

        if (supprimee) {
            return ResponseEntity.ok("Ville supprimée avec succès");
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Ville introuvable");
    }

    // Search by the beginning of a city name
    @GetMapping("/recherche/nom")
    public List<Ville> rechercherParNom(
            @RequestParam String debut) throws VilleException {

        List<Ville> resultat = villes.stream()
                .filter(ville -> ville.getNom()
                        .toLowerCase()
                        .startsWith(debut.toLowerCase()))
                .toList();

        if (resultat.isEmpty()) {
            throw new VilleException(
                    "Aucune ville dont le nom commence par "
                            + debut + " n'a été trouvée"
            );
        }

        return resultat;
    }

    // Search cities with a population greater than min
    @GetMapping("/recherche/population")
    public List<Ville> rechercherParPopulation(
            @RequestParam int min) throws VilleException {

        List<Ville> resultat = villes.stream()
                .filter(ville -> ville.getPopulation() > min)
                .toList();

        if (resultat.isEmpty()) {
            throw new VilleException(
                    "Aucune ville n'a une population supérieure à " + min
            );
        }

        return resultat;
    }

    // Search cities with a population between min and max
    @GetMapping("/recherche/intervalle")
    public List<Ville> rechercherParIntervalle(
            @RequestParam int min,
            @RequestParam int max) throws VilleException {

        List<Ville> resultat = villes.stream()
                .filter(ville -> ville.getPopulation() > min
                        && ville.getPopulation() < max)
                .toList();

        if (resultat.isEmpty()) {
            throw new VilleException(
                    "Aucune ville n'a une population comprise entre "
                            + min + " et " + max
            );
        }

        return resultat;
    }
}
