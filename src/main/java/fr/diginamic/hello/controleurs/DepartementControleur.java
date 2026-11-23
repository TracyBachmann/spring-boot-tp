package fr.diginamic.hello.controleurs;

import fr.diginamic.hello.dto.DepartementDto;
import fr.diginamic.hello.dto.VilleDto;
import fr.diginamic.hello.services.DepartementService;
import fr.diginamic.hello.services.ExportService;
import fr.diginamic.hello.services.VilleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departements")
@Tag(
        name = "Departments",
        description = "Operations for managing French departments"
)
public class DepartementControleur {

    private final DepartementService departementService;
    private final VilleService villeService;
    private final ExportService exportService;

    public DepartementControleur(
            DepartementService departementService,
            VilleService villeService,
            ExportService exportService) {

        this.departementService = departementService;
        this.villeService = villeService;
        this.exportService = exportService;
    }

    /**
     * Returns all departments.
     */
    @Operation(
            summary = "Get all departments",
            description = "Returns all departments stored in the database."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Departments retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(
                                    implementation = DepartementDto.class
                            )
                    )
            )
    )
    @GetMapping
    public List<DepartementDto> getDepartements() {
        return departementService.getDepartements();
    }

    /**
     * Returns a department by its identifier.
     */
    @Operation(summary = "Get a department by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Department found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = DepartementDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found"
            )
    })
    @GetMapping("/{id}")
    public DepartementDto getDepartement(
            @Parameter(
                    description = "Department identifier",
                    example = "39"
            )
            @PathVariable int id) {

        return departementService.getDepartement(id);
    }

    /**
     * Creates a department.
     */
    @Operation(summary = "Create a department")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Department created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data or duplicate department code"
            )
    })
    @PostMapping
    public DepartementDto ajouterDepartement(
            @Valid @RequestBody DepartementDto dto) {

        return departementService.ajouterDepartement(dto);
    }

    /**
     * Updates an existing department.
     */
    @Operation(summary = "Update a department")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Department updated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid department data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found"
            )
    })
    @PutMapping("/{id}")
    public DepartementDto modifierDepartement(
            @Parameter(
                    description = "Department identifier",
                    example = "39"
            )
            @PathVariable int id,
            @Valid @RequestBody DepartementDto dto) {

        return departementService.modifierDepartement(id, dto);
    }

    /**
     * Deletes a department without associated cities.
     */
    @Operation(
            summary = "Delete a department",
            description = "Deletes a department only if it has no cities."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Department deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Department still contains cities"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerDepartement(
            @Parameter(
                    description = "Department identifier",
                    example = "39"
            )
            @PathVariable int id) {

        departementService.supprimerDepartement(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the largest cities in a department.
     */
    @Operation(
            summary = "Get the largest cities in a department",
            description = "Returns up to n cities sorted by population."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cities retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid number of cities"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found"
            )
    })
    @GetMapping("/{id}/villes/top")
    public List<VilleDto> plusGrandesVilles(
            @Parameter(
                    description = "Department identifier",
                    example = "39"
            )
            @PathVariable int id,
            @Parameter(
                    description = "Maximum number of cities",
                    example = "3"
            )
            @RequestParam int n) {

        return villeService.plusGrandesVilles(id, n);
    }

    /**
     * Returns department cities above a population threshold.
     */
    @Operation(
            summary = "Get cities above a population threshold",
            description = "Returns cities with population strictly above min."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cities retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found"
            )
    })
    @GetMapping("/{id}/villes/min")
    public List<VilleDto> villesAuDessusPopulation(
            @Parameter(
                    description = "Department identifier",
                    example = "39"
            )
            @PathVariable int id,
            @Parameter(
                    description = "Minimum population (exclusive)",
                    example = "20000"
            )
            @RequestParam int min) {

        return villeService.villesAuDessusPopulation(id, min);
    }

    /**
     * Returns cities within a population interval.
     */
    @Operation(
            summary = "Get cities in a population interval",
            description = "Returns cities strictly between min and max."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cities retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid population interval"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found"
            )
    })
    @GetMapping("/{id}/villes")
    public List<VilleDto> villesParPopulation(
            @Parameter(
                    description = "Department identifier",
                    example = "39"
            )
            @PathVariable int id,
            @Parameter(
                    description = "Minimum population (exclusive)",
                    example = "20000"
            )
            @RequestParam int min,
            @Parameter(
                    description = "Maximum population (exclusive)",
                    example = "100000"
            )
            @RequestParam int max) {

        return villeService.villesParPopulationEtDepartement(
                id, min, max
        );
    }

    /**
     * Exports a department and its cities as a PDF document.
     */
    @Operation(
            summary = "Export a department to PDF",
            description = "Downloads the department name, code and city list."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "PDF generated successfully",
                    content = @Content(
                            mediaType = "application/pdf"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department code not found"
            )
    })
    @GetMapping(
            value = "/{code}/export/pdf",
            produces = "application/pdf"
    )
    public ResponseEntity<byte[]> exporterPdf(
            @Parameter(
                    description = "Department code, not its database ID",
                    example = "67"
            )
            @PathVariable String code) {

        byte[] contenu = exportService.exporterDepartementPdf(code);

        String nomFichier = "departement-"
                + code.replaceAll("[^A-Za-z0-9_-]", "_")
                + ".pdf";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nomFichier + "\""
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenu);
    }
}
