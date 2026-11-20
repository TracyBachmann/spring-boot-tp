package fr.diginamic.hello.aventures.modeles;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Aventure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nom;

    private int difficulte;

    @OneToMany(mappedBy = "aventure",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Etape> etapes = new ArrayList<>();

    public Aventure() {
    }

    public Aventure(String nom, int difficulte) {
        this.nom = nom;
        this.difficulte = difficulte;
    }

    public void ajouterEtape(Etape etape) {
        etapes.add(etape);
        etape.setAventure(this);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getDifficulte() {
        return difficulte;
    }

    public void setDifficulte(int difficulte) {
        this.difficulte = difficulte;
    }

    public List<Etape> getEtapes() {
        return etapes;
    }
}