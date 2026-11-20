package fr.diginamic.hello.repositories;

import fr.diginamic.hello.modeles.Ville;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VilleRepository
        extends JpaRepository<Ville, Integer> {

    List<Ville> findAllByOrderByIdAsc();

    boolean existsByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCaseAndIdNot(String nom, Integer id);

    boolean existsByDepartementId(Integer idDepartement);

    List<Ville> findByNomStartingWithIgnoreCase(String debut);

    List<Ville> findByPopulationGreaterThan(int min);

    List<Ville> findByPopulationGreaterThanAndPopulationLessThan(
            int min, int max);

    List<Ville> findByDepartementIdOrderByPopulationDescIdAsc(
            Integer idDepartement, Pageable pageable);

    List<Ville> findByDepartementIdAndPopulationBetweenOrderByPopulationDescIdAsc(
            Integer idDepartement, int min, int max);
}
