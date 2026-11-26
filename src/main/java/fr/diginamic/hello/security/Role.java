
package fr.diginamic.hello.security;

import org.springframework.security.core.GrantedAuthority;

public class Role implements GrantedAuthority {

    private final String nom;

    public Role(String nom) {
        this.nom = nom;
    }

    /**
     * Returns the authority name expected by Spring Security.
     *
     * @return authority name
     */
    @Override
    public String getAuthority() {
        return nom;
    }
}
