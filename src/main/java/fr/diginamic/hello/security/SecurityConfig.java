package fr.diginamic.hello.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * TP09 - Authentification JWT sans session HTTP.
 *
 * Sont conserves :
 * - utilisateurs et roles MariaDB (TP03)
 * - securite des methodes @Secured (TP06)
 * - Content Security Policy (TP07)
 * - configuration CORS (TP08)
 *
 * HTTP Basic et le formulaire de connexion sont
 * remplaces par une authentification JWT Bearer.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS : conserver le frontend local du TP08.
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

        // GET pour les villes.
        // POST permet aussi d'appeler /login depuis un frontend.
        configuration.setAllowedMethods(
                List.of("GET", "POST", "OPTIONS")
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
            HttpSecurity http,
            JwtUtil jwtUtil,
            JpaUserDetailsService userDetailsService)
            throws Exception {

        // Content Security Policy du TP07.
        String csp =
                "script-src 'self' https://cdn.jsdelivr.net; "
                        + "style-src 'self' 'unsafe-inline' "
                        + "https://cdn.jsdelivr.net "
                        + "https://cdnjs.cloudflare.com; "
                        + "img-src 'self'; "
                        + "font-src 'self' https://cdnjs.cloudflare.com;";

        // Filtre JWT cree explicitement.
        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(
                        jwtUtil,
                        userDetailsService
                );

        http
                // Conserver CORS du TP08.
                .cors(Customizer.withDefaults())

                // TP09 : pas de session, pas de CSRF
                // pour cette API authentifiee par Bearer.
                .csrf(csrf -> csrf.disable())

                // TP09 : aucune session HTTP d'authentification.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Desactiver les anciens mecanismes.
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())

                // Ne pas sauvegarder les requetes en session.
                .requestCache(cache -> cache.disable())

                // Garder la CSP du TP07.
                .headers(headers -> headers
                        .contentSecurityPolicy(policy -> policy
                                .policyDirectives(csp)
                        )
                )

                // Retourner 401 plutot que rediriger vers un login.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                (request, response, exception) ->
                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED
                                        )
                        )
                )

                // Seule la connexion POST est publique.
                // Les roles restent controles par @Secured.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.POST,
                                "/login"
                        ).permitAll()

                        .requestMatchers("/error").permitAll()

                        .anyRequest().authenticated()
                )

                // Executer notre filtre JWT avant
                // les filtres d'authentification habituels.
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
