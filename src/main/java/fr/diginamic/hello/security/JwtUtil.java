package fr.diginamic.hello.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * TP09 - Creation et verification des JWT.
 *
 * Les jetons expirent apres 15 minutes.
 */
@Component
public class JwtUtil {

    private static final long DUREE_VALIDITE_MS =
            15 * 60 * 1000L;

    private final SecretKey key;

    public JwtUtil(
            @Value("${JWT_SECRET:}") String secretBase64) {

        if (secretBase64 == null
                || secretBase64.isBlank()) {

            // Cle generee automatiquement pour les TP locaux.
            this.key = Jwts.SIG.HS256.key().build();

        } else {

            // Cle fournie depuis une variable d'environnement.
            this.key = Keys.hmacShaKeyFor(
                    Decoders.BASE64.decode(secretBase64)
            );
        }
    }

    /**
     * Cree un JWT signe contenant le nom de l'utilisateur.
     */
    public String generateToken(String username) {

        Date maintenant = new Date();

        Date expiration = new Date(
                maintenant.getTime() + DUREE_VALIDITE_MS
        );

        return Jwts.builder()
                .subject(username)
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * Verifie la signature et l'expiration du JWT.
     * Retourne son nom d'utilisateur.
     *
     * Un jeton invalide ou expire provoque une exception.
     */
    public String extractUsername(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }
}