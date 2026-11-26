package fr.diginamic.hello.security;

import fr.diginamic.hello.repositories.UtilisateurRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads authenticated users from the database.
 */
@Service
public class JpaUserDetailsService
        implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public JpaUserDetailsService(
            UtilisateurRepository utilisateurRepository) {

        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Loads a user and their roles using the username.
     *
     * @param username login identifier
     * @return matching user
     * @throws UsernameNotFoundException if the user does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {

        return utilisateurRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Unknown user"
                        )
                );
    }
}
