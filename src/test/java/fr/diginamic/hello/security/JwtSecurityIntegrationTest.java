package fr.diginamic.hello.security;

import fr.diginamic.hello.repositories.RoleRepository;
import fr.diginamic.hello.repositories.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises JWT authentication against the isolated H2 test database.
 */
@SpringBootTest
@AutoConfigureMockMvc
class JwtSecurityIntegrationTest {

    private static final String USERNAME = "jwt-test-user";
    private static final String ADMIN_USERNAME = "jwt-test-admin";
    private static final String PASSWORD = "test-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void createTestAccounts() {
        Role userRole = roleRepository.findByNom("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
        Role adminRole = roleRepository.findByNom("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        if (utilisateurRepository.findByUsername(USERNAME).isEmpty()) {
            utilisateurRepository.save(new Utilisateur(
                    USERNAME,
                    passwordEncoder.encode(PASSWORD),
                    List.of(userRole)
            ));
        }

        if (utilisateurRepository.findByUsername(ADMIN_USERNAME).isEmpty()) {
            utilisateurRepository.save(new Utilisateur(
                    ADMIN_USERNAME,
                    passwordEncoder.encode(PASSWORD),
                    List.of(adminRole)
            ));
        }
    }

    @Test
    void anonymousRequestIsRejected() throws Exception {
        mockMvc.perform(get("/villes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"jwt-test-user","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidBearerTokenIsRejected() throws Exception {
        mockMvc.perform(get("/villes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer FAKE_TOKEN"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userTokenCanReadCitiesWithoutCreatingSession() throws Exception {
        String token = login(USERNAME);
        assertThat(token.split("\\.")).hasSize(3);

        mockMvc.perform(get("/villes")
                        .param("page", "0")
                        .param("size", "2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    void adminTokenCanReadCities() throws Exception {
        String token = login(ADMIN_USERNAME);

        mockMvc.perform(get("/villes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void userTokenCannotDeleteCities() throws Exception {
        String token = login(USERNAME);

        mockMvc.perform(delete("/villes/999999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminTokenReachesServiceOnDelete() throws Exception {
        String token = login(ADMIN_USERNAME);

        // Authorization succeeds; the requested city does not exist.
        mockMvc.perform(delete("/villes/999999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void corsPreflightAllowsLocalFrontend() throws Exception {
        mockMvc.perform(options("/villes")
                        .header(HttpHeaders.ORIGIN, "http://127.0.0.1:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://127.0.0.1:4200"));
    }

    private String login(String username) throws Exception {
        String json = """
                {"username":"%s","password":"%s"}
                """.formatted(username, PASSWORD);

        return mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}