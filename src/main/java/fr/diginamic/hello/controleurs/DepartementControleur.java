package fr.diginamic.hello.controleurs;

import fr.diginamic.hello.dto.DepartementDto;
import fr.diginamic.hello.dto.VilleDto;
import fr.diginamic.hello.services.DepartementService;
import fr.diginamic.hello.services.VilleService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departements")
public class DepartementControleur {

    private final DepartementService departementService;
    private final VilleService villeService;

    public DepartementControleur(
            DepartementService departementService,
            VilleService villeService) {
        this.departementService = departementService;
        this.villeService = villeService;
    }

    /**
     * Returns all departments.
     */
    @GetMapping
    public List<DepartementDto> getDepartements() {
        return departementService.getDepartements();
    }

    /**
     * Returns a department by its identifier.
     */
    @GetMapping("/{id}")
    public DepartementDto getDepartement(@PathVariable int id) {
        return departementService.getDepartement(id);
    }

    /**
     * Creates a department.
     */
    @PostMapping
    public DepartementDto ajouterDepartement(
            @Valid @RequestBody DepartementDto dto) {
        return departementService.ajouterDepartement(dto);
    }

    /**
     * Updates a department.
     */
    @PutMapping("/{id}")
    public DepartementDto modifierDepartement(
            @PathVariable int id,
            @Valid @RequestBody DepartementDto dto) {
        return departementService.modifierDepartement(id, dto);
    }

    /**
     * Deletes a department if it has no cities.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerDepartement(
            @PathVariable int id) {
        departementService.supprimerDepartement(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the n largest cities in a department.
     */
    @GetMapping("/{id}/villes/top")
    public List<VilleDto> plusGrandesVilles(
            @PathVariable int id,
            @RequestParam int n) {
        return villeService.plusGrandesVilles(id, n);
    }

    /**
     * Returns cities in a given population interval.
     */
    @GetMapping("/{id}/villes")
    public List<VilleDto> villesParPopulation(
            @PathVariable int id,
            @RequestParam int min,
            @RequestParam int max) {
        return villeService.villesParPopulationEtDepartement(
                id, min, max
        );
    }
}
