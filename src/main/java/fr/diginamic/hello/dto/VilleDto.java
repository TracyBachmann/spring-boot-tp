package fr.diginamic.hello.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VilleDto {

    private Integer id;

    @NotNull(message = "Le nom est obligatoire")
    @Size(min = 2, message = "Le nom doit contenir au moins 2 caractères")
    private String nom;

    @Min(value = 1, message = "La population doit être supérieure ou égale à 1")
    private int population;

    private String codeDepartement;
    private Integer idDepartement;

    public VilleDto() {
    }

    public VilleDto(Integer id, String nom, int population,
                    String codeDepartement, Integer idDepartement) {
        this.id = id;
        this.nom = nom;
        this.population = population;
        this.codeDepartement = codeDepartement;
        this.idDepartement = idDepartement;
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

    public String getCodeDepartement() {
        return codeDepartement;
    }

    public void setCodeDepartement(String codeDepartement) {
        this.codeDepartement = codeDepartement;
    }

    public Integer getIdDepartement() {
        return idDepartement;
    }

    public void setIdDepartement(Integer idDepartement) {
        this.idDepartement = idDepartement;
    }
}
