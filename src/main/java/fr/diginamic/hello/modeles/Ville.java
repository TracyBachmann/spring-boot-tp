package fr.diginamic.hello.modeles;

import jakarta.persistence.*;

@Entity
@Table(name = "ville")
public class Ville {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nom;

    @Column(name = "nb_habs", nullable = false)
    private int population;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_dept", nullable = false)
    private Departement departement;

    public Ville() {
    }

    public Ville(String nom, int population, Departement departement) {
        this.nom = nom;
        this.population = population;
        this.departement = departement;
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

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public Departement getDepartement() {
        return departement;
    }

    public void setDepartement(Departement departement) {
        this.departement = departement;
    }
}
