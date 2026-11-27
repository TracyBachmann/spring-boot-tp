package fr.diginamic.hello.security;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * TP09 - Verification du JWT dans l'en-tete Authorization.
 *
 * Ce filtre est ajoute explicitement dans SecurityConfig.
 */
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService) {

        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader(
                "Authorization"
        );

        // Sans jeton Bearer, on laisse Spring Security
        // verifier si la route est accessible.
        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();

        try {

            // Verifie la signature et l'expiration du JWT.
            String username = jwtUtil.extractUsername(token);

            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException(
                        "JWT sans nom d'utilisateur"
                );
            }

            // Recharge l'utilisateur et ses roles depuis MariaDB.
            UserDetails utilisateur =
                    userDetailsService.loadUserByUsername(
                            username
                    );

            // Construit l'authentification pour Spring Security.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            utilisateur,
                            null,
                            utilisateur.getAuthorities()
                    );

            SecurityContext contexte =
                    SecurityContextHolder.createEmptyContext();

            contexte.setAuthentication(authentication);

            SecurityContextHolder.setContext(contexte);

        } catch (JwtException
                 | IllegalArgumentException
                 | AuthenticationException exception) {

            SecurityContextHolder.clearContext();

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Jeton JWT invalide ou expire"
            );

            return;
        }

        filterChain.doFilter(request, response);
    }
}
