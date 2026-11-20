package fr.diginamic.hello.services;

import fr.diginamic.hello.dto.VilleDto;
import fr.diginamic.hello.modeles.Departement;
import fr.diginamic.hello.modeles.Ville;
import fr.diginamic.hello.repositories.DepartementRepository;
import fr.diginamic.hello.repositories.VilleRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class VilleService {

    private final VilleRepository villeRepository;
    private final DepartementRepository departementRepository;

    public VilleService(VilleRepository villeRepository,
                        DepartementRepository departementRepository) {
        this.villeRepository = villeRepository;
        this.departementRepository = departementRepository;
    }

    /**
     * Converts a city entity into a DTO.
     *
     * @param ville city to convert
     * @return DTO containing city and department information
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
     * Returns all cities.
     */
    @Transactional(readOnly = true)
    public List<VilleDto> getVilles() {
        return toDtoList(villeRepository.findAllByOrderByIdAsc());
    }

    /**
     * Returns a city by its identifier.
     */
    @Transactional(readOnly = true)
    public VilleDto getVille(int id) {
        return toDto(trouverVille(id));
    }

    private Ville trouverVille(int id) {
        return villeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ville introuvable"
                ));
    }

    /**
     * Finds or creates the department associated with a city.
     */
    private Departement trouverDepartement(VilleDto dto) {
        String code = dto.getCodeDepartement();

        if (code != null) {
            code = code.trim().toUpperCase(Locale.ROOT);
        }

        boolean codePresent = code != null && !code.isBlank();
        Integer id = dto.getIdDepartement();

        if (id != null) {
            Departement departement =
                    departementRepository.findById(id).orElse(null);

            if (departement != null) {
                if (codePresent && !departement.getCode().equalsIgnoreCase(code)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Le code et l'identifiant du département ne correspondent pas"
                    );
                }

                return departement;
            }

            if (!codePresent) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Département inconnu"
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
                    HttpStatus.BAD_REQUEST, "Code département trop long"
            );
        }

        String codeFinal = code;

        return departementRepository.findByCodeIgnoreCase(codeFinal)
                .orElseGet(() -> departementRepository.save(
                        new Departement(codeFinal, "À compléter (" + codeFinal + ")")
                ));
    }

    private void verifierNom(VilleDto dto) {
        if (dto.getNom() == null || dto.getNom().isBlank()
                || dto.getNom().trim().length() < 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le nom doit contenir au moins 2 caractères"
            );
        }
    }

    /**
     * Creates a city linked to a department.
     */
    @Transactional
    public VilleDto ajouterVille(VilleDto dto) {
        verifierNom(dto);
        String nom = dto.getNom().trim();

        if (villeRepository.existsByNomIgnoreCase(nom)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La ville existe déjà"
            );
        }

        Departement departement = trouverDepartement(dto);

        Ville ville = new Ville(nom, dto.getPopulation(), departement);
        return toDto(villeRepository.saveAndFlush(ville));
    }

    /**
     * Updates an existing city.
     */
    @Transactional
    public VilleDto modifierVille(int id, VilleDto dto) {
        Ville ville = trouverVille(id);
        verifierNom(dto);

        String nom = dto.getNom().trim();

        if (villeRepository.existsByNomIgnoreCaseAndIdNot(nom, id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La ville existe déjà"
            );
        }

        Departement departement = trouverDepartement(dto);

        ville.setNom(nom);
        ville.setPopulation(dto.getPopulation());
        ville.setDepartement(departement);

        return toDto(villeRepository.saveAndFlush(ville));
    }

    /**
     * Deletes a city.
     */
    @Transactional
    public void supprimerVille(int id) {
        Ville ville = trouverVille(id);
        villeRepository.delete(ville);
    }

    @Transactional(readOnly = true)
    public List<VilleDto> rechercherParNom(String debut) {
        return toDtoList(
                villeRepository.findByNomStartingWithIgnoreCase(debut)
        );
    }

    @Transactional(readOnly = true)
    public List<VilleDto> rechercherParPopulation(int min) {
        return toDtoList(
                villeRepository.findByPopulationGreaterThan(min)
        );
    }

    @Transactional(readOnly = true)
    public List<VilleDto> rechercherParIntervalle(int min, int max) {
        if (min >= max) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Intervalle invalide"
            );
        }

        return toDtoList(
                villeRepository
                        .findByPopulationGreaterThanAndPopulationLessThan(
                                min, max)
        );
    }

    /**
     * Returns the largest cities in a department.
     */
    @Transactional(readOnly = true)
    public List<VilleDto> plusGrandesVilles(int departementId, int n) {
        verifierDepartement(departementId);

        if (n < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "n doit être supérieur à 0"
            );
        }

        return toDtoList(
                villeRepository.findByDepartementIdOrderByPopulationDescIdAsc(
                        departementId, PageRequest.of(0, n)
                )
        );
    }

    /**
     * Returns cities within a population interval and department.
     */
    @Transactional(readOnly = true)
    public List<VilleDto> villesParPopulationEtDepartement(
            int departementId, int min, int max) {

        verifierDepartement(departementId);

        if (min > max) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Intervalle invalide"
            );
        }

        return toDtoList(
                villeRepository
                        .findByDepartementIdAndPopulationBetweenOrderByPopulationDescIdAsc(
                                departementId, min, max)
        );
    }

    private void verifierDepartement(int id) {
        if (!departementRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Département inconnu"
            );
        }
    }
}
