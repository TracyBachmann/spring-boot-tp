package fr.diginamic.hello.services;

import fr.diginamic.hello.dto.DepartementDto;
import fr.diginamic.hello.modeles.Departement;
import fr.diginamic.hello.repositories.DepartementRepository;
import fr.diginamic.hello.repositories.VilleRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class DepartementService {

    private final DepartementRepository departementRepository;
    private final VilleRepository villeRepository;

    public DepartementService(
            DepartementRepository departementRepository,
            VilleRepository villeRepository) {
        this.departementRepository = departementRepository;
        this.villeRepository = villeRepository;
    }

    private DepartementDto toDto(Departement departement) {
        return new DepartementDto(
                departement.getId(),
                departement.getCode(),
                departement.getNom()
        );
    }

    private Departement trouverDepartement(int id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Département inconnu"
                ));
    }

    /**
     * Returns all departments.
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
     */
    @Transactional(readOnly = true)
    public DepartementDto getDepartement(int id) {
        return toDto(trouverDepartement(id));
    }

    /**
     * Creates a department.
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

    private String normaliserCode(String code) {
        if (code == null || code.isBlank() || code.trim().length() > 10) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Code département invalide"
            );
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }
}
