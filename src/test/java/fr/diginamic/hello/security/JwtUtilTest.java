package fr.diginamic.hello.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    @Test
    void generatedTokenContainsExpectedUsername() {
        JwtUtil jwtUtil = new JwtUtil("");
        String token = jwtUtil.generateToken("demo-user");

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("demo-user");
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        JwtUtil issuer = new JwtUtil("");
        JwtUtil verifier = new JwtUtil("");
        String token = issuer.generateToken("demo-user");

        assertThatThrownBy(() -> verifier.extractUsername(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        // Fixed test key; never use it for the running application.
        byte[] keyBytes = "0123456789abcdef0123456789abcdef"
                .getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        String secretBase64 = Base64.getEncoder().encodeToString(keyBytes);
        JwtUtil jwtUtil = new JwtUtil(secretBase64);

        long now = System.currentTimeMillis();
        String token = Jwts.builder()
                .subject("demo-user")
                .issuedAt(new Date(now - 120_000L))
                .expiration(new Date(now - 60_000L))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> jwtUtil.extractUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
