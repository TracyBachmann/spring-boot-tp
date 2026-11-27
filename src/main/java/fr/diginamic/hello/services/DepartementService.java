package fr.diginamic.hello.services;

import com.fasterxml.jackson.databind.JsonNode;

import fr.diginamic.hello.dto.DepartementDto;
import fr.diginamic.hello.modeles.Departement;
import fr.diginamic.hello.repositories.DepartementRepository;
import fr.diginamic.hello.repositories.VilleRepository;
import fr.diginamic.hello.security.AuditSupport;

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
     * Converts a department into a DTO.
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
     */
    @Transactional(readOnly = true)
    public List<DepartementDto> getDepartements() {

        return departementRepository
                .findAllByOrderByIdAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Returns a department by its identifier.
     */
    @Transactional(readOnly = true)
    public DepartementDto getDepartement(int id) {

        return toDto(trouverDepartement(id));
    }

    /**
     * Creates a department and records its author.
     */
    @Transactional
    public DepartementDto ajouterDepartement(
            DepartementDto dto) {

        String code = normaliserCode(dto.getCode());

        if (departementRepository
                .existsByCodeIgnoreCase(code)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ce département existe déjà"
            );
        }

        Departement departement = new Departement(
                code,
                dto.getNom().trim()
        );

        // TP04 - Record the authenticated user and date.
        AuditSupport.signer(departement);

        Departement enregistre =
                departementRepository.save(departement);

        // TP04 - Log the operation.
        LOGGER.info(
                "DEPARTEMENT CREATION id={} utilisateur={}",
                enregistre.getId(),
                enregistre.getUserMaj()
        );

        return toDto(enregistre);
    }

    /**
     * Updates a department and records its author.
     */
    @Transactional
    public DepartementDto modifierDepartement(
            int id,
            DepartementDto dto) {

        Departement departement = trouverDepartement(id);
        String code = normaliserCode(dto.getCode());

        departementRepository
                .findByCodeIgnoreCase(code)
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

        // TP04 - Update the modification signature.
        AuditSupport.signer(departement);

        Departement enregistre =
                departementRepository.save(departement);

        LOGGER.info(
                "DEPARTEMENT MODIFICATION id={} utilisateur={}",
                enregistre.getId(),
                enregistre.getUserMaj()
        );

        return toDto(enregistre);
    }

    /**
     * Deletes a department if no cities belong to it.
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

        LOGGER.info(
                "DEPARTEMENT SUPPRESSION id={} utilisateur={}",
                id,
                AuditSupport.utilisateurCourant()
        );
    }

    /**
     * Normalizes and validates a department code.
     */
    private String normaliserCode(String code) {

        if (code == null
                || code.isBlank()
                || code.trim().length() > 10) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Code département invalide"
            );
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Initializes existing department names
     * using the French government API.
     *
     * The feature remains disabled when application.init=false.
     */
    @Transactional
    public void initData() {

        if (!initialisationActive) {

            LOGGER.info(
                    "Department initialization disabled (application.init=false)."
            );

            return;
        }

        LOGGER.info(
                "Starting department initialization from the external API."
        );

        RestTemplate restTemplate = new RestTemplate();
        JsonNode departementsApi;

        try {

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

        if (departementsApi == null
                || !departementsApi.isArray()) {

            throw new IllegalStateException(
                    "Invalid response from the department API"
            );
        }

        List<Departement> departementsAMettreAJour =
                new ArrayList<>();

        int departementsCorrespondants = 0;

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
                // Preserve existing census identifiers.
                continue;
            }

            departementsCorrespondants++;

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

        departementRepository
                .saveAll(departementsAMettreAJour);

        LOGGER.info(
                "Department initialization completed: {} matched, {} updated.",
                departementsCorrespondants,
                departementsAMettreAJour.size()
        );
    }
}
