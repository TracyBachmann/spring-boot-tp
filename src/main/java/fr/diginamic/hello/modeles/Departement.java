package fr.diginamic.hello.modeles;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departement")
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    // The supplied census SQL contains departments without a name.
    @Column(nullable = true)
    private String nom;

    @OneToMany(mappedBy = "departement")
    private List<Ville> villes = new ArrayList<>();

    public Departement() {
    }

    public Departement(String code, String nom) {
        this.code = code;
        this.nom = nom;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<Ville> getVilles() {
        return villes;
    }
}