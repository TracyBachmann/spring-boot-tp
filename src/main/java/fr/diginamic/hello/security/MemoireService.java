package fr.diginamic.hello.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Stores two demonstration users in memory.
 *
 * Passwords are read from application configuration and
 * encoded with BCrypt before being stored in memory.
 */
@Service
public class MemoireService implements UserDetailsService {

    private final Map<String, Utilisateur> utilisateurs;

    public MemoireService(
            PasswordEncoder passwordEncoder,
            @Value("${security.demo.user-password:}")
            String userPassword,
            @Value("${security.demo.admin-password:}")
            String adminPassword) {

        if (userPassword == null
                || userPassword.isBlank()
                || adminPassword == null
                || adminPassword.isBlank()) {

            throw new IllegalStateException(
                    "Define SECURITY_USER_PASSWORD and "
                            + "SECURITY_ADMIN_PASSWORD before "
                            + "starting TP02."
            );
        }

        // Encode the demonstration passwords with BCrypt.
        String encodedUserPassword =
                passwordEncoder.encode(userPassword);

        String encodedAdminPassword =
                passwordEncoder.encode(adminPassword);

        // Create a standard user and an administrator.
        Utilisateur utilisateur = new Utilisateur(
                "demo-user",
                encodedUserPassword,
                List.of(new Role("ROLE_USER"))
        );

        Utilisateur administrateur = new Utilisateur(
                "demo-admin",
                encodedAdminPassword,
                List.of(new Role("ROLE_ADMIN"))
        );

        // Store users by their login identifier.
        utilisateurs = Map.of(
                utilisateur.getUsername(), utilisateur,
                administrateur.getUsername(), administrateur
        );
    }

    /**
     * Loads a user by username for HTTP Basic authentication.
     *
     * @param username login identifier
     * @return matching user details
     */
    @Override
    public UserDetails loadUserByUsername(String username) {

        Utilisateur utilisateur = utilisateurs.get(username);

        if (utilisateur == null) {
            throw new UsernameNotFoundException(
                    "Unknown user: " + username
            );
        }

        return utilisateur;
    }
}
