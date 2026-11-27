package fr.diginamic.hello.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents an application user persisted with JPA.
 */
@Entity
@Table(name = "sec_utilisateur")
public class Utilisateur implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String username;

    @Column(
            name = "password_hash",
            nullable = false,
            length = 100
    )
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "sec_utilisateur_role",
            joinColumns = @JoinColumn(
                    name = "utilisateur_id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id"
            )
    )
    private List<Role> roles = new ArrayList<>();

    /**
     * Required by JPA.
     */
    protected Utilisateur() {
    }

    public Utilisateur(
            String username,
            String password,
            List<Role> roles) {

        this.username = username;
        this.password = password;
        this.roles = new ArrayList<>(roles);
    }

    public Long getId() {
        return id;
    }

    public List<Role> getRoles() {
        return roles;
    }

    /**
     * Returns the roles used for authorization.
     *
     * @return granted authorities
     */
    @Override
    public Collection<Role> getAuthorities() {
        return roles;
    }

    /**
     * Returns the BCrypt password hash.
     *
     * @return encoded password
     */
    @Override
    public String getPassword() {
        return password;
    }

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
