package fr.diginamic.hello.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * TP09 - Informations envoyees pour se connecter.
 */
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public LoginRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
