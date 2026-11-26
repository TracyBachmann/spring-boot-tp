package fr.diginamic.hello.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;

/**
 * Represents a security role stored in the database.
 */
@Entity
@Table(name = "sec_role")
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "nom",
            nullable = false,
            unique = true,
            length = 30
    )
    private String nom;

    /**
     * Required by JPA.
     */
    protected Role() {
    }

    public Role(String nom) {
        this.nom = nom;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    /**
     * Returns the authority recognized by Spring Security.
     *
     * @return role name, such as ROLE_USER
     */
    @Override
    public String getAuthority() {
        return nom;
    }
}
