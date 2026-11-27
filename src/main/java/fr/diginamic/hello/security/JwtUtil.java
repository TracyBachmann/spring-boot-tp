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
 * Signs JWTs and validates their signature and expiration.
 */
@Component
public class JwtUtil {

    private static final long TOKEN_VALIDITY_MS = 15 * 60 * 1000L;

    private final SecretKey key;

    public JwtUtil(@Value("${JWT_SECRET:}") String secretBase64) {
        if (secretBase64 == null || secretBase64.isBlank()) {
            // Suitable for local development; tokens expire on restart.
            this.key = Jwts.SIG.HS256.key().build();
        } else {
            this.key = Keys.hmacShaKeyFor(
                    Decoders.BASE64.decode(secretBase64)
            );
        }
    }

    public String generateToken(String username) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(
                issuedAt.getTime() + TOKEN_VALIDITY_MS
        );

        return Jwts.builder()
                .subject(username)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }
}
