package fr.diginamic.hello.repositories;

import fr.diginamic.hello.modeles.Ville;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VilleRepository extends JpaRepository<Ville, Integer> {

    boolean existsByNomIgnoreCaseAndDepartementId(String nom, Integer departementId);

    boolean existsByNomIgnoreCaseAndDepartementIdAndIdNot(
            String nom, Integer departementId, Integer id);

    boolean existsByDepartementId(Integer departementId);

    List<Ville> findByNomStartingWithIgnoreCase(String debut);

    List<Ville> findByPopulationGreaterThanOrderByPopulationDesc(int min);

    List<Ville> findByPopulationGreaterThanAndPopulationLessThanOrderByPopulationDesc(
            int min, int max);

    List<Ville> findByDepartementIdAndPopulationGreaterThanOrderByPopulationDesc(
            Integer departementId, int min);

    List<Ville> findByDepartementIdAndPopulationGreaterThanAndPopulationLessThanOrderByPopulationDesc(
            Integer departementId, int min, int max);

    List<Ville> findByDepartementIdOrderByPopulationDescIdAsc(
            Integer departementId, Pageable pageable);
}