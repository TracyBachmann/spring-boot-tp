package fr.diginamic.hello.services;

import com.fasterxml.jackson.databind.JsonNode;
import fr.diginamic.hello.dto.DepartementDto;
import fr.diginamic.hello.modeles.Departement;
import fr.diginamic.hello.repositories.DepartementRepository;
import fr.diginamic.hello.repositories.VilleRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DepartementService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DepartementService.class);

    private static final String API_DEPARTEMENTS =
            "https://geo.api.gouv.fr/departements";

    private final DepartementRepository departementRepository;
    private final VilleRepository villeRepository;

    @Value("${application.init:false}")
    private boolean initialisationActive;

    public DepartementService(
            DepartementRepository departementRepository,
            VilleRepository villeRepository) {

        this.departementRepository = departementRepository;
        this.villeRepository = villeRepository;
    }

    /**
     * Converts a department entity into a DTO.
     *
     * @param departement department to convert
     * @return department DTO
     */
    private DepartementDto toDto(Departement departement) {
        return new DepartementDto(
                departement.getId(),
                departement.getCode(),
                departement.getNom()
        );
    }

    /**
     * Finds a department by its identifier.
     *
     * @param id department identifier
     * @return matching department entity
     */
    private Departement trouverDepartement(int id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Département inconnu"
                ));
    }

    /**
     * Returns all departments.
     *
     * @return list of department DTOs
     */
    @Transactional(readOnly = true)
    public List<DepartementDto> getDepartements() {
        return departementRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Returns a department by its identifier.
     *
     * @param id department identifier
     * @return department DTO
     */
    @Transactional(readOnly = true)
    public DepartementDto getDepartement(int id) {
        return toDto(trouverDepartement(id));
    }

    /**
     * Creates a new department.
     *
     * @param dto department data
     * @return created department DTO
     */
    @Transactional
    public DepartementDto ajouterDepartement(DepartementDto dto) {
        String code = normaliserCode(dto.getCode());

        if (departementRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ce département existe déjà"
            );
        }

        Departement departement = new Departement(
                code, dto.getNom().trim()
        );

        return toDto(departementRepository.save(departement));
    }

    /**
     * Updates an existing department.
     *
     * @param id department identifier
     * @param dto updated department data
     * @return updated department DTO
     */
    @Transactional
    public DepartementDto modifierDepartement(
            int id, DepartementDto dto) {

        Departement departement = trouverDepartement(id);
        String code = normaliserCode(dto.getCode());

        departementRepository.findByCodeIgnoreCase(code)
                .ifPresent(autre -> {
                    if (!autre.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Ce code est déjà utilisé"
                        );
                    }
                });

        departement.setCode(code);
        departement.setNom(dto.getNom().trim());

        return toDto(departementRepository.save(departement));
    }

    /**
     * Deletes a department without associated cities.
     *
     * @param id department identifier
     */
    @Transactional
    public void supprimerDepartement(int id) {
        Departement departement = trouverDepartement(id);

        if (villeRepository.existsByDepartementId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de supprimer un département contenant des villes"
            );
        }

        departementRepository.delete(departement);
    }

    /**
     * Normalizes and validates a department code.
     *
     * @param code department code
     * @return normalized code
     */
    private String normaliserCode(String code) {
        if (code == null || code.isBlank()
                || code.trim().length() > 10) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Code département invalide"
            );
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Initializes department names using the French government API.
     *
     * The initialization is skipped when application.init is false.
     * Existing department identifiers and city relationships are preserved.
     */
    @Transactional
    public void initData() {

        // Skip initialization when the configuration flag is disabled.
        if (!initialisationActive) {
            LOGGER.info(
                    "Department initialization disabled (application.init=false)."
            );
            return;
        }

        LOGGER.info("Starting department initialization from the external API.");

        RestTemplate restTemplate = new RestTemplate();

        JsonNode departementsApi;

        try {
            // Retrieve official department data in JSON format.
            departementsApi = restTemplate.getForObject(
                    API_DEPARTEMENTS,
                    JsonNode.class
            );
        } catch (RestClientException exception) {
            LOGGER.error(
                    "Unable to retrieve departments from the external API.",
                    exception
            );

            throw new IllegalStateException(
                    "Department initialization failed: external API unavailable",
                    exception
            );
        }

        if (departementsApi == null || !departementsApi.isArray()) {
            throw new IllegalStateException(
                    "Invalid response from the department API"
            );
        }

        List<Departement> departementsAMettreAJour =
                new ArrayList<>();

        int departementsCorrespondants = 0;

        // Match official departments with existing database records by code.
        for (JsonNode departementApi : departementsApi) {

            String code = departementApi
                    .path("code")
                    .asText("")
                    .trim();

            String nom = departementApi
                    .path("nom")
                    .asText("")
                    .trim();

            if (code.isBlank() || nom.isBlank()) {
                continue;
            }

            Departement departement =
                    departementRepository
                            .findByCodeIgnoreCase(code)
                            .orElse(null);

            if (departement == null) {
                // Do not insert new departments into the census database.
                continue;
            }

            departementsCorrespondants++;

            // Update only when the official name differs.
            if (!nom.equals(departement.getNom())) {
                departement.setNom(nom);
                departementsAMettreAJour.add(departement);
            }
        }

        if (departementsCorrespondants == 0) {
            throw new IllegalStateException(
                    "No matching departments found in the database"
            );
        }

        // Persist changes without replacing IDs or city relationships.
        departementRepository.saveAll(departementsAMettreAJour);

        LOGGER.info(
                "Department initialization completed: {} matched, {} updated.",
                departementsCorrespondants,
                departementsAMettreAJour.size()
        );
    }
}
