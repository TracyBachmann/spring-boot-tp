package fr.diginamic.hello.security;

import fr.diginamic.hello.repositories.RoleRepository;
import fr.diginamic.hello.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Creates demonstration accounts in the database when enabled.
 *
 * Initialization is disabled by default and existing accounts
 * are not overwritten.
 */
@Component
public class SecurityUsersInitializer
        implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.seed.enabled:false}")
    private boolean initialisationActive;

    @Value("${security.seed.user-password:}")
    private String userPassword;

    @Value("${security.seed.admin-password:}")
    private String adminPassword;

    public SecurityUsersInitializer(
            RoleRepository roleRepository,
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder) {

        this.roleRepository = roleRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates roles and users only when initialization is enabled.
     *
     * @param args application startup arguments
     */
    @Override
    @Transactional
    public void run(String... args) {

        if (!initialisationActive) {
            return;
        }

        if (userPassword.isBlank()
                || adminPassword.isBlank()) {

            throw new IllegalStateException(
                    "Set SECURITY_USER_PASSWORD and "
                            + "SECURITY_ADMIN_PASSWORD before "
                            + "initializing database users."
            );
        }

        // Reuse existing roles when available.
        Role roleUser = roleRepository
                .findByNom("ROLE_USER")
                .orElseGet(() -> roleRepository.save(
                        new Role("ROLE_USER")
                ));

        Role roleAdmin = roleRepository
                .findByNom("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(
                        new Role("ROLE_ADMIN")
                ));

        // Create the standard user only if it does not exist.
        if (utilisateurRepository
                .findByUsername("demo-user")
                .isEmpty()) {

            Utilisateur utilisateur = new Utilisateur(
                    "demo-user",
                    passwordEncoder.encode(userPassword),
                    List.of(roleUser)
            );

            utilisateurRepository.save(utilisateur);
        }

        // Create the administrator only if it does not exist.
        if (utilisateurRepository
                .findByUsername("demo-admin")
                .isEmpty()) {

            Utilisateur administrateur = new Utilisateur(
                    "demo-admin",
                    passwordEncoder.encode(adminPassword),
                    List.of(roleAdmin)
            );

            utilisateurRepository.save(administrateur);
        }

        // Never log plaintext passwords or password hashes.
    }
}
