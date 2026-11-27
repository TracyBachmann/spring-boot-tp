package fr.diginamic.hello.controleurs;

import fr.diginamic.hello.dto.LoginRequest;
import fr.diginamic.hello.security.JwtUtil;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * TP09 - Connexion et generation des JWT.
 */
@RestController
public class LoginController {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;

    public LoginController(
            AuthenticationConfiguration authenticationConfiguration,
            JwtUtil jwtUtil) {

        this.authenticationConfiguration =
                authenticationConfiguration;

        this.jwtUtil = jwtUtil;
    }

    /**
     * Verifie les identifiants dans MariaDB,
     * puis retourne un JWT sous forme de texte.
     */
    @PostMapping(
            value = "/login",
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String login(
            @Valid @RequestBody LoginRequest requete)
            throws Exception {

        try {

            authenticationConfiguration
                    .getAuthenticationManager()
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    requete.getUsername(),
                                    requete.getPassword()
                            )
                    );

        } catch (AuthenticationException exception) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Identifiants incorrects"
            );
        }

        return jwtUtil.generateToken(
                requete.getUsername()
        );
    }
}
