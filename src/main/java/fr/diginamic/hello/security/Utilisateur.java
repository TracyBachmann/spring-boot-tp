package fr.diginamic.hello.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Represents an authenticated application user.
 *
 * This class provides Spring Security with the username,
 * encoded password, authorities and account status.
 */
public class Utilisateur implements UserDetails {

    private final String username;
    private final String password;
    private final List<Role> roles;

    public Utilisateur(
            String username,
            String password,
            List<Role> roles) {

        this.username = username;
        this.password = password;
        this.roles = List.copyOf(roles);
    }

    /**
     * Returns the authorities assigned to the user.
     *
     * @return user authorities
     */
    @Override
    public Collection<Role> getAuthorities() {
        return roles;
    }

    /**
     * Returns the encoded password.
     *
     * @return encoded password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the login identifier.
     *
     * @return username
     */
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
