package fr.diginamic.hello.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * TP05 - Demonstration of HTTP sessions and JSESSIONID.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // Keep HTTP Basic available for API clients.
                .httpBasic(Customizer.withDefaults())

                // Enable the Spring Security login form.
                .formLogin(Customizer.withDefaults())

                // Create an HTTP session when necessary.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                // Configure access permissions.
                .authorizeHttpRequests(auth -> auth

                        // Allow the login page and error page.
                        .requestMatchers(
                                "/login",
                                "/error"
                        ).permitAll()

                        // GET requests require ROLE_USER.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/**"
                        ).hasRole("USER")

                        // Other methods require ROLE_ADMIN.
                        .anyRequest().hasRole("ADMIN")
                );

        // CSRF protection stays enabled for browser login.
        return http.build();
    }
}
