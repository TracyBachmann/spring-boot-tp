package fr.diginamic.hello.security;

import fr.diginamic.hello.modeles.Departement;
import fr.diginamic.hello.modeles.Ville;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

/**
 * Utility class for recording the authenticated user
 * and the modification date on database entities.
 */
public final class AuditSupport {

    private AuditSupport() {
    }

    /**
     * Returns the current authenticated username.
     *
     * For automated tests and non-HTTP operations,
     * returns "system" when no user is authenticated.
     */
    public static String utilisateurCourant() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            return "system";
        }

        return authentication.getName();
    }

    /**
     * Records the user and modification date on a city.
     */
    public static void signer(Ville ville) {

        ville.setUserMaj(utilisateurCourant());
        ville.setDateMaj(LocalDateTime.now());
    }

    /**
     * Records the user and modification date on a department.
     */
    public static void signer(Departement departement) {

        departement.setUserMaj(utilisateurCourant());
        departement.setDateMaj(LocalDateTime.now());
    }
}