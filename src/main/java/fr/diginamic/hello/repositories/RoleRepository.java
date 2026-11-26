package fr.diginamic.hello.repositories;

import fr.diginamic.hello.security.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Provides database access for security roles.
 */
public interface RoleRepository
        extends JpaRepository<Role, Long> {

    Optional<Role> findByNom(String nom);
}
