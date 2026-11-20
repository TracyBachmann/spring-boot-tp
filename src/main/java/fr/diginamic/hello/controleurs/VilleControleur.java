package fr.diginamic.hello.controleurs;

import fr.diginamic.hello.dto.VilleDto;
import fr.diginamic.hello.services.VilleService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/villes")
public class VilleControleur {

    private final VilleService villeService;

    public VilleControleur(VilleService villeService) {
        this.villeService = villeService;
    }

    /**
     * Returns all cities.
     */
    @GetMapping
    public List<VilleDto> getVilles() {
        return villeService.getVilles();
    }

    /**
     * Returns a city by its identifier.
     */
    @GetMapping("/{id}")
    public VilleDto getVille(@PathVariable int id) {
        return villeService.getVille(id);
    }

    /**
     * Creates a city linked to a department.
     */
    @PostMapping
    public VilleDto ajouterVille(@Valid @RequestBody VilleDto ville) {
        return villeService.ajouterVille(ville);
    }

    /**
     * Updates a city.
     */
    @PutMapping("/{id}")
    public VilleDto modifierVille(
            @PathVariable int id,
            @Valid @RequestBody VilleDto ville) {
        return villeService.modifierVille(id, ville);
    }

    /**
     * Deletes a city.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerVille(@PathVariable int id) {
        villeService.supprimerVille(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Searches cities by the beginning of their name.
     */
    @GetMapping("/recherche/nom")
    public List<VilleDto> rechercherParNom(
            @RequestParam String debut) {
        return villeService.rechercherParNom(debut);
    }

    /**
     * Searches cities above a population threshold.
     */
    @GetMapping("/recherche/population")
    public List<VilleDto> rechercherParPopulation(
            @RequestParam int min) {
        return villeService.rechercherParPopulation(min);
    }

    /**
     * Searches cities within a population interval.
     */
    @GetMapping("/recherche/intervalle")
    public List<VilleDto> rechercherParIntervalle(
            @RequestParam int min,
            @RequestParam int max) {
        return villeService.rechercherParIntervalle(min, max);
    }
}
