package fr.diginamic.hello.services;

import fr.diginamic.hello.dto.VilleDto;
import fr.diginamic.hello.modeles.Departement;
import fr.diginamic.hello.modeles.Ville;
import fr.diginamic.hello.repositories.DepartementRepository;
import fr.diginamic.hello.repositories.VilleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(VilleService.class)
class VilleServiceH2Test {

    @Autowired
    private VilleService villeService;

    @Autowired
    private VilleRepository villeRepository;

    @Autowired
    private DepartementRepository departementRepository;

    private Departement basRhin;
    private Departement paris;

    private Ville strasbourg;
    private Ville haguenau;
    private Ville parisVille;

    @BeforeEach
    void setUp() {

        // Create departments in the isolated H2 database.
        basRhin = departementRepository.saveAndFlush(
                new Departement("67", "Bas-Rhin")
        );

        paris = departementRepository.saveAndFlush(
                new Departement("75", "Paris")
        );

        // Create three cities for the test scenarios.
        strasbourg = villeRepository.saveAndFlush(
                new Ville("Strasbourg", 279284, basRhin)
        );

        haguenau = villeRepository.saveAndFlush(
                new Ville("Haguenau", 35000, basRhin)
        );

        parisVille = villeRepository.saveAndFlush(
                new Ville("Paris", 2100000, paris)
        );
    }

    /**
     * Verifies that an entity is correctly converted to a DTO.
     */
    @Test
    void toDto_shouldReturnCityInformation() {

        VilleDto dto = villeService.toDto(strasbourg);

        assertEquals("Strasbourg", dto.getNom());
        assertEquals(279284, dto.getPopulation());
        assertEquals("67", dto.getCodeDepartement());
        assertEquals(basRhin.getId(), dto.getIdDepartement());
    }

    /**
     * Verifies that the city list is paginated.
     */
    @Test
    void getVilles_shouldReturnPaginatedResults() {

        Page<VilleDto> result = villeService.getVilles(0, 2);

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    /**
     * Rejects invalid pagination parameters.
     */
    @Test
    void getVilles_shouldRejectInvalidPagination() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> villeService.getVilles(-1, 0)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );
    }

    /**
     * Finds an existing city by its identifier.
     */
    @Test
    void getVille_shouldFindExistingCity() {

        VilleDto result = villeService.getVille(
                strasbourg.getId()
        );

        assertEquals("Strasbourg", result.getNom());
        assertEquals("67", result.getCodeDepartement());
    }

    /**
     * Rejects an unknown city identifier.
     */
    @Test
    void getVille_shouldRejectUnknownCity() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> villeService.getVille(999999)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    /**
     * Creates a city in an existing department.
     */
    @Test
    void ajouterVille_shouldCreateCity() {

        VilleDto dto = new VilleDto(
                null,
                "Saverne",
                11000,
                "67",
                basRhin.getId()
        );

        VilleDto result = villeService.ajouterVille(dto);

        assertNotNull(result.getId());
        assertEquals("Saverne", result.getNom());
        assertEquals("67", result.getCodeDepartement());
        assertEquals(4, villeRepository.count());
    }

    /**
     * Prevents duplicate city names in the same department.
     */
    @Test
    void ajouterVille_shouldRejectDuplicateInSameDepartment() {

        VilleDto dto = new VilleDto(
                null,
                "Strasbourg",
                280000,
                "67",
                basRhin.getId()
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> villeService.ajouterVille(dto)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(3, villeRepository.count());
    }

    /**
     * Allows the same city name in a different department.
     */
    @Test
    void ajouterVille_shouldAllowSameNameInAnotherDepartment() {

        VilleDto dto = new VilleDto(
                null,
                "Strasbourg",
                2000,
                "75",
                paris.getId()
        );

        VilleDto result = villeService.ajouterVille(dto);

        assertNotNull(result.getId());
        assertEquals("75", result.getCodeDepartement());
        assertEquals(4, villeRepository.count());
    }

    /**
     * Updates the population of an existing city.
     */
    @Test
    void modifierVille_shouldUpdateCity() {

        VilleDto dto = new VilleDto(
                strasbourg.getId(),
                "Strasbourg",
                300000,
                "67",
                basRhin.getId()
        );

        VilleDto result = villeService.modifierVille(
                strasbourg.getId(), dto
        );

        assertEquals(300000, result.getPopulation());

        Ville updated = villeRepository.findById(
                strasbourg.getId()
        ).orElseThrow();

        assertEquals(300000, updated.getPopulation());
    }

    /**
     * Rejects a duplicate name during an update.
     */
    @Test
    void modifierVille_shouldRejectDuplicate() {

        VilleDto dto = new VilleDto(
                haguenau.getId(),
                "Strasbourg",
                35000,
                "67",
                basRhin.getId()
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> villeService.modifierVille(
                        haguenau.getId(), dto
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );
    }

    /**
     * Deletes an existing city.
     */
    @Test
    void supprimerVille_shouldDeleteCity() {

        Integer id = haguenau.getId();

        villeService.supprimerVille(id);

        assertFalse(villeRepository.existsById(id));
        assertEquals(2, villeRepository.count());
    }

    /**
     * Searches city names without case sensitivity.
     */
    @Test
    void rechercherParNom_shouldMatchPrefixIgnoringCase() {

        List<VilleDto> result =
                villeService.rechercherParNom("stra");

        assertEquals(1, result.size());
        assertEquals("Strasbourg", result.get(0).getNom());
    }

    /**
     * Returns cities above the specified population.
     */
    @Test
    void rechercherParPopulation_shouldReturnDescendingResults() {

        List<VilleDto> result =
                villeService.rechercherParPopulation(100000);

        assertEquals(2, result.size());
        assertEquals("Paris", result.get(0).getNom());
        assertEquals("Strasbourg", result.get(1).getNom());
    }

    /**
     * Returns cities inside an exclusive population interval.
     */
    @Test
    void rechercherParIntervalle_shouldFilterPopulation() {

        List<VilleDto> result =
                villeService.rechercherParIntervalle(
                        30000, 300000
                );

        assertEquals(2, result.size());
        assertEquals("Strasbourg", result.get(0).getNom());
        assertEquals("Haguenau", result.get(1).getNom());
    }

    /**
     * Rejects an invalid population interval.
     */
    @Test
    void rechercherParIntervalle_shouldRejectInvalidRange() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> villeService.rechercherParIntervalle(
                        100000, 1000
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );
    }

    /**
     * Finds the largest cities in a given department.
     */
    @Test
    void plusGrandesVilles_shouldReturnTopCities() {

        List<VilleDto> result =
                villeService.plusGrandesVilles(
                        basRhin.getId(), 1
                );

        assertEquals(1, result.size());
        assertEquals("Strasbourg", result.get(0).getNom());
    }

    /**
     * Rejects a top count of zero.
     */
    @Test
    void plusGrandesVilles_shouldRejectZero() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> villeService.plusGrandesVilles(
                        basRhin.getId(), 0
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );
    }

    /**
     * Filters cities using both department and population.
     */
    @Test
    void villesParPopulationEtDepartement_shouldFilterCities() {

        List<VilleDto> result =
                villeService.villesParPopulationEtDepartement(
                        basRhin.getId(), 30000, 300000
                );

        assertEquals(2, result.size());
        assertEquals("Strasbourg", result.get(0).getNom());
        assertEquals("Haguenau", result.get(1).getNom());
    }

    /**
     * Returns only cities above the minimum population.
     */
    @Test
    void villesAuDessusPopulation_shouldFilterCities() {

        List<VilleDto> result =
                villeService.villesAuDessusPopulation(
                        basRhin.getId(), 100000
                );

        assertEquals(1, result.size());
        assertEquals("Strasbourg", result.get(0).getNom());
    }

    /**
     * Rejects an unknown department.
     */
    @Test
    void villesAuDessusPopulation_shouldRejectUnknownDepartment() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> villeService.villesAuDessusPopulation(
                        999999, 1000
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }
}