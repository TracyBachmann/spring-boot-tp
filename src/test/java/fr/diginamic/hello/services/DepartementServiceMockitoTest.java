package fr.diginamic.hello.services;

import fr.diginamic.hello.dto.DepartementDto;
import fr.diginamic.hello.modeles.Departement;
import fr.diginamic.hello.repositories.DepartementRepository;
import fr.diginamic.hello.repositories.VilleRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartementServiceMockitoTest {

    @Mock
    private DepartementRepository departementRepository;

    @Mock
    private VilleRepository villeRepository;

    @InjectMocks
    private DepartementService departementService;

    /**
     * Returns the list of departments from mocked repository data.
     */
    @Test
    void getDepartements_shouldReturnAllDepartments() {

        Departement basRhin =
                new Departement("67", "Bas-Rhin");
        basRhin.setId(1);

        Departement paris =
                new Departement("75", "Paris");
        paris.setId(2);

        when(departementRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(basRhin, paris));

        List<DepartementDto> result =
                departementService.getDepartements();

        assertEquals(2, result.size());
        assertEquals("Bas-Rhin", result.get(0).getNom());
        assertEquals("75", result.get(1).getCode());

        verify(departementRepository)
                .findAllByOrderByIdAsc();
    }

    /**
     * Finds an existing department using its identifier.
     */
    @Test
    void getDepartement_shouldFindExistingDepartment() {

        Departement departement =
                new Departement("67", "Bas-Rhin");
        departement.setId(1);

        when(departementRepository.findById(1))
                .thenReturn(Optional.of(departement));

        DepartementDto result =
                departementService.getDepartement(1);

        assertEquals(1, result.getId());
        assertEquals("67", result.getCode());
        assertEquals("Bas-Rhin", result.getNom());
    }

    /**
     * Rejects a department that does not exist.
     */
    @Test
    void getDepartement_shouldRejectUnknownDepartment() {

        when(departementRepository.findById(999))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departementService.getDepartement(999)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    /**
     * Creates a department when its code is available.
     */
    @Test
    void ajouterDepartement_shouldCreateDepartment() {

        DepartementDto dto = new DepartementDto(
                null, "67", "Bas-Rhin"
        );

        when(departementRepository.existsByCodeIgnoreCase("67"))
                .thenReturn(false);

        when(departementRepository.save(any(Departement.class)))
                .thenAnswer(invocation -> {
                    Departement saved = invocation.getArgument(0);
                    saved.setId(1);
                    return saved;
                });

        DepartementDto result =
                departementService.ajouterDepartement(dto);

        assertEquals(1, result.getId());
        assertEquals("67", result.getCode());
        assertEquals("Bas-Rhin", result.getNom());

        verify(departementRepository)
                .save(any(Departement.class));
    }

    /**
     * Rejects a duplicate department code.
     */
    @Test
    void ajouterDepartement_shouldRejectDuplicateCode() {

        DepartementDto dto = new DepartementDto(
                null, "67", "Bas-Rhin"
        );

        when(departementRepository.existsByCodeIgnoreCase("67"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departementService.ajouterDepartement(dto)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(departementRepository, never())
                .save(any(Departement.class));
    }

    /**
     * Updates an existing department.
     */
    @Test
    void modifierDepartement_shouldUpdateDepartment() {

        Departement existing =
                new Departement("67", "Old name");
        existing.setId(1);

        DepartementDto dto = new DepartementDto(
                1, "67", "Bas-Rhin"
        );

        when(departementRepository.findById(1))
                .thenReturn(Optional.of(existing));

        when(departementRepository.findByCodeIgnoreCase("67"))
                .thenReturn(Optional.of(existing));

        when(departementRepository.save(any(Departement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DepartementDto result =
                departementService.modifierDepartement(1, dto);

        assertEquals("Bas-Rhin", result.getNom());
        assertEquals("67", result.getCode());

        verify(departementRepository)
                .save(existing);
    }

    /**
     * Rejects an update when the code belongs to another department.
     */
    @Test
    void modifierDepartement_shouldRejectCodeConflict() {

        Departement existing =
                new Departement("67", "Bas-Rhin");
        existing.setId(1);

        Departement another =
                new Departement("75", "Paris");
        another.setId(2);

        DepartementDto dto = new DepartementDto(
                1, "75", "New name"
        );

        when(departementRepository.findById(1))
                .thenReturn(Optional.of(existing));

        when(departementRepository.findByCodeIgnoreCase("75"))
                .thenReturn(Optional.of(another));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departementService.modifierDepartement(1, dto)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(departementRepository, never())
                .save(any(Departement.class));
    }

    /**
     * Deletes a department with no associated cities.
     */
    @Test
    void supprimerDepartement_shouldDeleteEmptyDepartment() {

        Departement existing =
                new Departement("67", "Bas-Rhin");
        existing.setId(1);

        when(departementRepository.findById(1))
                .thenReturn(Optional.of(existing));

        when(villeRepository.existsByDepartementId(1))
                .thenReturn(false);

        departementService.supprimerDepartement(1);

        verify(departementRepository).delete(existing);
    }

    /**
     * Prevents deletion when the department still contains cities.
     */
    @Test
    void supprimerDepartement_shouldRejectDepartmentWithCities() {

        Departement existing =
                new Departement("67", "Bas-Rhin");
        existing.setId(1);

        when(departementRepository.findById(1))
                .thenReturn(Optional.of(existing));

        when(villeRepository.existsByDepartementId(1))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departementService.supprimerDepartement(1)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(departementRepository, never())
                .delete(any(Departement.class));
    }

    /**
     * Rejects an invalid department code.
     */
    @Test
    void ajouterDepartement_shouldRejectBlankCode() {

        DepartementDto dto = new DepartementDto(
                null, " ", "Test"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departementService.ajouterDepartement(dto)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(departementRepository);
    }

    /**
     * Does not call the external API when initialization is disabled.
     */
    @Test
    void initData_shouldDoNothingWhenDisabled() {

        ReflectionTestUtils.setField(
                departementService,
                "initialisationActive",
                false
        );

        assertDoesNotThrow(
                () -> departementService.initData()
        );

        verifyNoInteractions(
                departementRepository,
                villeRepository
        );
    }
}