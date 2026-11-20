package fr.diginamic.hello.repositories;

import fr.diginamic.hello.modeles.Departement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartementRepository
        extends JpaRepository<Departement, Integer> {

    Optional<Departement> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Departement> findAllByOrderByIdAsc();
}
