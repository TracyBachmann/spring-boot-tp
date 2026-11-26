package fr.diginamic.hello.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures HTTP Basic authentication and role-based access.
 */
@Configuration
public class SecurityConfig {

    /**
     * Creates the password encoder used by the user service.
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures access rules for the REST API.
     *
     * GET requests require ROLE_USER.
     * Other HTTP methods require ROLE_ADMIN.
     *
     * @param http Spring Security HTTP configuration
     * @return configured security filter chain
     * @throws Exception when security configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // Disable CSRF for this HTTP Basic training exercise.
                .csrf(csrf -> csrf.disable())

                // Enable HTTP Basic authentication.
                .httpBasic(Customizer.withDefaults())

                // Apply the role-based access rules.
                .authorizeHttpRequests(auth -> auth

                        // GET requests require ROLE_USER.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/**"
                        ).hasRole("USER")

                        // POST, PUT, DELETE and other methods
                        // require ROLE_ADMIN.
                        .anyRequest().hasRole("ADMIN")
                );

        return http.build();
    }
}
