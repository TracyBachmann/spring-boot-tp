package fr.diginamic.hello.controleurs;

import fr.diginamic.hello.modeles.Ville;
import jakarta.annotation.PostConstruct;
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

    // Add a new city
    @PostMapping
    public ResponseEntity<String> ajouterVille(@RequestBody Ville ville) {
        for (Ville villeExistante : villes) {
            if (villeExistante.getNom().equalsIgnoreCase(ville.getNom())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("La ville existe déjà");
            }
        }

        ville.setId(prochainId++);
        villes.add(ville);

        return ResponseEntity.ok("Ville insérée avec succès");
    }

    // Update an existing city
    @PutMapping("/{id}")
    public ResponseEntity<String> modifierVille(
            @PathVariable int id,
            @RequestBody Ville villeModifiee) {

        for (Ville ville : villes) {
            if (ville.getId() == id) {
                ville.setNom(villeModifiee.getNom());
                ville.setPopulation(villeModifiee.getPopulation());

                return ResponseEntity.ok("Ville modifiée avec succès");
            }
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Ville introuvable");
    }

    // Delete an existing city
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerVille(@PathVariable int id) {
        for (Ville ville : villes) {
            if (ville.getId() == id) {
                villes.remove(ville);
                return ResponseEntity.ok("Ville supprimée avec succès");
            }
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Ville introuvable");
    }
}
