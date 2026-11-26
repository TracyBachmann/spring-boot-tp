package fr.diginamic.hello.services;

import fr.diginamic.hello.dto.VilleDto;
import fr.diginamic.hello.modeles.Departement;
import fr.diginamic.hello.modeles.Ville;
import fr.diginamic.hello.repositories.DepartementRepository;
import fr.diginamic.hello.repositories.VilleRepository;
import fr.diginamic.hello.security.AuditSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class VilleService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(VilleService.class);

    private final VilleRepository villeRepository;
    private final DepartementRepository departementRepository;

    public VilleService(
            VilleRepository villeRepository,
            DepartementRepository departementRepository) {

        this.villeRepository = villeRepository;
        this.departementRepository = departementRepository;
    }

    /**
     * Converts a city entity into a DTO.
     */
    public VilleDto toDto(Ville ville) {

        Departement departement = ville.getDepartement();

        return new VilleDto(
                ville.getId(),
                ville.getNom(),
                ville.getPopulation(),
                departement.getCode(),
                departement.getId()
        );
    }

    private List<VilleDto> toDtoList(List<Ville> villes) {
        return villes.stream().map(this::toDto).toList();
    }

    /**
     * Returns a paginated list of cities.
     */
    @Transactional(readOnly = true)
    public Page<VilleDto> getVilles(int page, int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Pagination invalide (size entre 1 et 100)"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").ascending()
        );

        return villeRepository.findAll(pageable)
                .map(this::toDto);
    }

    /**
     * Returns one city by its identifier.
     */
    @Transactional(readOnly = true)
    public VilleDto getVille(int id) {
        return toDto(trouverVille(id));
    }

    private Ville trouverVille(int id) {

        return villeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ville introuvable"
                ));
    }

    /**
     * Finds an existing department, or creates one when
     * only an unknown department code is supplied.
     */
    private Departement trouverDepartement(VilleDto dto) {

        String code = dto.getCodeDepartement();

        if (code != null) {
            code = code.trim().toUpperCase(Locale.ROOT);
        }

        boolean codePresent =
                code != null && !code.isBlank();

        Integer id = dto.getIdDepartement();

        if (id != null) {

            Departement departement =
                    departementRepository
                            .findById(id)
                            .orElse(null);

            if (departement != null) {

                if (codePresent
                        && !departement.getCode()
                        .equalsIgnoreCase(code)) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Le code et l'identifiant du département ne correspondent pas"
                    );
                }

                return departement;
            }

            if (!codePresent) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Département inconnu"
                );
            }
        }

        if (!codePresent) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le code ou l'identifiant du département est obligatoire"
            );
        }

        if (code.length() > 10) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Code département trop long"
            );
        }

        String codeFinal = code;

        return departementRepository
                .findByCodeIgnoreCase(codeFinal)
                .orElseGet(() -> {

                    Departement nouveau = new Departement(
                            codeFinal,
                            "À compléter (" + codeFinal + ")"
                    );

                    // An automatically created department
                    // must also receive an audit signature.
                    AuditSupport.signer(nouveau);

                    Departement enregistre =
                            departementRepository.save(nouveau);

                    LOGGER.info(
                            "DEPARTEMENT CREATION id={} utilisateur={}",
                            enregistre.getId(),
                            enregistre.getUserMaj()
                    );

                    return enregistre;
                });
    }

    /**
     * Validates the city name.
     */
    private void verifierNom(VilleDto dto) {

        if (dto.getNom() == null
                || dto.getNom().isBlank()
                || dto.getNom().trim().length() < 2) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le nom doit contenir au moins 2 caractères"
            );
        }
    }

    /**
     * Creates a new city and records its author.
     */
    @Transactional
    public VilleDto ajouterVille(VilleDto dto) {

        verifierNom(dto);

        String nom = dto.getNom().trim();
        Departement departement = trouverDepartement(dto);

        if (villeRepository
                .existsByNomIgnoreCaseAndDepartementId(
                        nom,
                        departement.getId()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La ville existe déjà dans ce département"
            );
        }

        Ville ville = new Ville(
                nom,
                dto.getPopulation(),
                departement
        );

        // TP04 - Record the authenticated user and date.
        AuditSupport.signer(ville);

        Ville enregistree =
                villeRepository.saveAndFlush(ville);

        // TP04 - Write the operation to the log file.
        LOGGER.info(
                "VILLE CREATION id={} utilisateur={}",
                enregistree.getId(),
                enregistree.getUserMaj()
        );

        return toDto(enregistree);
    }

    /**
     * Updates an existing city and records its author.
     */
    @Transactional
    public VilleDto modifierVille(int id, VilleDto dto) {

        Ville ville = trouverVille(id);

        verifierNom(dto);

        String nom = dto.getNom().trim();
        Departement departement = trouverDepartement(dto);

        if (villeRepository
                .existsByNomIgnoreCaseAndDepartementIdAndIdNot(
                        nom,
                        departement.getId(),
                        id
                )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La ville existe déjà dans ce département"
            );
        }

        ville.setNom(nom);
        ville.setPopulation(dto.getPopulation());
        ville.setDepartement(departement);

        // TP04 - Update the signature.
        AuditSupport.signer(ville);

        Ville enregistree =
                villeRepository.saveAndFlush(ville);

        LOGGER.info(
                "VILLE MODIFICATION id={} utilisateur={}",
                enregistree.getId(),
                enregistree.getUserMaj()
        );

        return toDto(enregistree);
    }

    /**
     * Deletes a city and logs the operation.
     */
    @Transactional
    public void supprimerVille(int id) {

        Ville ville = trouverVille(id);

        villeRepository.delete(ville);

        LOGGER.info(
                "VILLE SUPPRESSION id={} utilisateur={}",
                id,
                AuditSupport.utilisateurCourant()
        );
    }

    /**
     * Searches cities by name prefix.
     */
    @Transactional(readOnly = true)
    public List<VilleDto> rechercherParNom(String debut) {

        return toDtoList(
                villeRepository.findByNomStartingWithIgnoreCase(debut)
        );
    }

    /**
     * Searches cities above a population threshold.
     */
    @Transactional(readOnly = true)
    public List<VilleDto> rechercherParPopulation(int min) {

        return toDtoList(
                villeRepository
                        .findByPopulationGreaterThanOrderByPopulationDesc(min)
        );
    }

    /**
     * Searches cities inside a population interval.
     */
    @Transactional(readOnly = true)
    public List<VilleDto> rechercherParIntervalle(
            int min, int max) {

        if (min >= max) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Intervalle invalide"
            );
        }

        return toDtoList(
                villeRepository
                        .findByPopulationGreaterThanAndPopulationLessThanOrderByPopulationDesc(
                                min,
                                max
                        )
        );
    }

    /**
     * Returns the largest cities in a department.
     */
    @Transactional(readOnly = true)
    public List<VilleDto> plusGrandesVilles(
            int departementId,
            int n) {

        verifierDepartement(departementId);

        if (n < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "n doit être supérieur à 0"
            );
        }

        return toDtoList(
                villeRepository
                        .findByDepartementIdOrderByPopulationDescIdAsc(
                                departementId,
                                PageRequest.of(0, n)
                        )
        );
    }

    /**
     * Returns cities in a population interval
     * within a given department.
     */
    @Transactional(readOnly = true)
    public List<VilleDto> villesParPopulationEtDepartement(
            int departementId,
            int min,
            int max) {

        verifierDepartement(departementId);

        if (min >= max) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Intervalle invalide"
            );
        }

        return toDtoList(
                villeRepository
                        .findByDepartementIdAndPopulationGreaterThanAndPopulationLessThanOrderByPopulationDesc(
                                departementId,
                                min,
                                max
                        )
        );
    }

    /**
     * Returns department cities above a threshold.
     */
    @Transactional(readOnly = true)
    public List<VilleDto> villesAuDessusPopulation(
            int departementId,
            int min) {

        verifierDepartement(departementId);

        return toDtoList(
                villeRepository
                        .findByDepartementIdAndPopulationGreaterThanOrderByPopulationDesc(
                                departementId,
                                min
                        )
        );
    }

    /**
     * Verifies that a department exists.
     */
    private void verifierDepartement(int id) {

        if (!departementRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Département inconnu"
            );
        }
    }
}
