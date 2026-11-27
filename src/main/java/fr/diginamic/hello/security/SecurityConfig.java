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

/**
 * TP06 - Security at the method level.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // Keep CSRF protection for browser sessions.
                // Allow Basic-authenticated API clients to send
                // POST, PUT and DELETE during this exercise.
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

                // Keep HTTP Basic authentication for Postman.
                .httpBasic(Customizer.withDefaults())

                // Keep the login form from TP05.
                .formLogin(form -> form.permitAll())

                // Keep browser sessions available.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                // TP06: all application routes require a login.
                // Roles are checked with @Secured on methods.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
