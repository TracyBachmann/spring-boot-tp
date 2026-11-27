package fr.diginamic.hello.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * TP08 - Configuration CORS definitive.
 *
 * Les protections des TP precedents sont conservees :
 * - TP03 : utilisateurs et roles dans MariaDB
 * - TP05 : formulaire de connexion et sessions HTTP
 * - TP06 : autorisations par @Secured
 * - TP07 : Content Security Policy
 * - TP08 : autorisation CORS du frontend local
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Autorise uniquement les deux adresses locales
     * utilisees pour la page HTML du professeur.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:4200",
                        "http://127.0.0.1:4200"
                )
        );

        configuration.setAllowedMethods(
                List.of("GET", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type")
        );

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        // Politique CSP du TP07.
        String csp =
                "script-src 'self' https://cdn.jsdelivr.net; "
                        + "style-src 'self' 'unsafe-inline' "
                        + "https://cdn.jsdelivr.net "
                        + "https://cdnjs.cloudflare.com; "
                        + "img-src 'self'; "
                        + "font-src 'self' https://cdnjs.cloudflare.com;";

        http
                // TP08 : CORS reste active.
                .cors(Customizer.withDefaults())

                // Protection CSRF conservee depuis le TP06.
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        request -> {
                            String authorization =
                                    request.getHeader(
                                            HttpHeaders.AUTHORIZATION
                                    );

                            return authorization != null
                                    && authorization.startsWith("Basic ");
                        }
                ))

                // Authentification HTTP Basic pour les API.
                .httpBasic(Customizer.withDefaults())

                // Formulaire de connexion du TP05.
                .formLogin(form -> form.permitAll())

                // Sessions HTTP du TP05.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                // Content Security Policy du TP07.
                .headers(headers -> headers
                        .contentSecurityPolicy(policy -> policy
                                .policyDirectives(csp)
                        )
                )

                // SECURITE RETABLIE :
                // plus aucun acces public temporaire a /villes.
                // Les roles sont controles avec @Secured.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
