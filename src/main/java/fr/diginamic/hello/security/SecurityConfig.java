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
 * TP07 - Content Security Policy configuration.
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

        // Content Security Policy based on the course example.
        String csp =
                "script-src 'self' https://cdn.jsdelivr.net; "
                        + "style-src 'self' 'unsafe-inline' "
                        + "https://cdn.jsdelivr.net "
                        + "https://cdnjs.cloudflare.com; "
                        + "img-src 'self'; "
                        + "font-src 'self' https://cdnjs.cloudflare.com;";

        http
                // Preserve TP06 CSRF handling for Basic Auth clients.
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

                // Preserve API authentication.
                .httpBasic(Customizer.withDefaults())

                // Preserve the browser login form from TP05.
                .formLogin(form -> form.permitAll())

                // Preserve HTTP sessions from TP05.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                // TP07 - Send the Content-Security-Policy header.
                .headers(headers -> headers
                        .contentSecurityPolicy(policy -> policy
                                .policyDirectives(csp)
                        )
                )

                // Preserve TP06: authentication is required.
                // Roles are checked with @Secured on methods.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
