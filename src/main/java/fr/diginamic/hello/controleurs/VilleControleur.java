package fr.diginamic.hello.controleurs;

import fr.diginamic.hello.dto.VilleDto;
import fr.diginamic.hello.services.ExportService;
import fr.diginamic.hello.services.VilleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/villes")
@Tag(
        name = "Cities",
        description = "Operations for managing and searching French cities"
)
public class VilleControleur {

    private final VilleService villeService;
    private final ExportService exportService;

    public VilleControleur(
            VilleService villeService,
            ExportService exportService) {

        this.villeService = villeService;
        this.exportService = exportService;
    }

    /**
     * Returns a paginated list of cities.
     */
    @Operation(
            summary = "Get paginated cities",
            description = "Returns cities ordered by identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters"
            )
    })
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping
    public Page<VilleDto> getVilles(
            @Parameter(
                    description = "Page number starting from zero",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,
            @Parameter(
                    description = "Page size between 1 and 100",
                    example = "20"
            )
            @RequestParam(defaultValue = "20") int size) {

        return villeService.getVilles(page, size);
    }

    /**
     * Returns a city by its identifier.
     */
    @Operation(summary = "Get a city by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "City found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = VilleDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "City not found"
            )
    })
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping("/{id}")
    public VilleDto getVille(
            @Parameter(
                    description = "City identifier",
                    example = "1"
            )
            @PathVariable int id) {

        return villeService.getVille(id);
    }

    /**
     * Creates a city linked to a department.
     */
    @Operation(summary = "Create a city")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "City created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid city data or duplicate city"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found"
            )
    })
    @Secured("ROLE_ADMIN")
    @PostMapping
    public VilleDto ajouterVille(
            @Valid @RequestBody VilleDto ville) {

        return villeService.ajouterVille(ville);
    }

    /**
     * Updates an existing city.
     */
    @Operation(summary = "Update a city")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "City updated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid city data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found"
            )
    })
    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    public VilleDto modifierVille(
            @Parameter(
                    description = "City identifier",
                    example = "1"
            )
            @PathVariable int id,
            @Valid @RequestBody VilleDto ville) {

        return villeService.modifierVille(id, ville);
    }

    /**
     * Deletes a city.
     */
    @Operation(summary = "Delete a city")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "City deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "City not found"
            )
    })
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerVille(
            @Parameter(
                    description = "City identifier",
                    example = "1"
            )
            @PathVariable int id) {

        villeService.supprimerVille(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Searches cities by the beginning of their name.
     */
    @Operation(summary = "Search cities by name")
    @ApiResponse(
            responseCode = "200",
            description = "Search completed successfully"
    )
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping("/recherche/nom")
    public List<VilleDto> rechercherParNom(
            @Parameter(
                    description = "Beginning of the city name",
                    example = "Stra"
            )
            @RequestParam String debut) {

        return villeService.rechercherParNom(debut);
    }

    /**
     * Searches cities above a population threshold.
     */
    @Operation(
            summary = "Search cities by minimum population"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Search completed successfully"
    )
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping("/recherche/population")
    public List<VilleDto> rechercherParPopulation(
            @Parameter(
                    description = "Minimum population (exclusive)",
                    example = "100000"
            )
            @RequestParam int min) {

        return villeService.rechercherParPopulation(min);
    }

    /**
     * Searches cities within a population interval.
     */
    @Operation(
            summary = "Search cities by population interval"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid population interval"
            )
    })
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping("/recherche/intervalle")
    public List<VilleDto> rechercherParIntervalle(
            @Parameter(
                    description = "Minimum population (exclusive)",
                    example = "20000"
            )
            @RequestParam int min,
            @Parameter(
                    description = "Maximum population (exclusive)",
                    example = "50000"
            )
            @RequestParam int max) {

        return villeService.rechercherParIntervalle(min, max);
    }

    /**
     * Exports cities above a population threshold as CSV.
     */
    @Operation(
            summary = "Export cities to CSV",
            description = "Downloads cities with a population greater than min."
    )
    @ApiResponse(
            responseCode = "200",
            description = "CSV file generated successfully",
            content = @Content(
                    mediaType = "text/csv"
            )
    )
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping(
            value = "/export/csv",
            produces = "text/csv"
    )
    public ResponseEntity<byte[]> exporterCsv(
            @Parameter(
                    description = "Minimum population (exclusive)",
                    example = "100000"
            )
            @RequestParam int min) {

        byte[] contenu = exportService.exporterVillesCsv(min);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"villes.csv\""
                )
                .contentType(
                        new MediaType(
                                "text",
                                "csv",
                                java.nio.charset.StandardCharsets.UTF_8
                        )
                )
                .body(contenu);
    }
}
