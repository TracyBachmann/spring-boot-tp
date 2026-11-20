package fr.diginamic.hello.dto;

import jakarta.validation.constraints.NotBlank;

public class DepartementDto {

    private Integer id;

    @NotBlank(message = "Le code du département est obligatoire")
    private String code;

    @NotBlank(message = "Le nom du département est obligatoire")
    private String nom;

    public DepartementDto() {
    }

    public DepartementDto(Integer id, String code, String nom) {
        this.id = id;
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
}
