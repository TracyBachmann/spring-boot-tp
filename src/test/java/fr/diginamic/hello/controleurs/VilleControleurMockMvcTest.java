package fr.diginamic.hello.controleurs;

import fr.diginamic.hello.dto.VilleDto;
import fr.diginamic.hello.services.ExportService;
import fr.diginamic.hello.services.VilleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Controller validation tests; SecurityFilterChain is tested separately. */
@WebMvcTest(VilleControleur.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "ADMIN")
class VilleControleurMockMvcTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private VilleService villeService;
    @MockitoBean private ExportService exportService;

    @Test
    void ajouterVille_shouldReturn200ForValidData() throws Exception {
        VilleDto created = new VilleDto(1, "Saverne", 11000, "67", 39);
        when(villeService.ajouterVille(any(VilleDto.class))).thenReturn(created);
        mockMvc.perform(post("/villes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"nom":"Saverne","population":11000,
                         "codeDepartement":"67","idDepartement":39}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Saverne"))
                .andExpect(jsonPath("$.population").value(11000))
                .andExpect(jsonPath("$.codeDepartement").value("67"));
        verify(villeService).ajouterVille(any(VilleDto.class));
    }

    @Test
    void ajouterVille_shouldRejectShortName() throws Exception {
        mockMvc.perform(post("/villes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"A\",\"population\":11000,\"codeDepartement\":\"67\"}"))
                .andExpect(status().isBadRequest());
        verify(villeService, never()).ajouterVille(any(VilleDto.class));
    }

    @Test
    void ajouterVille_shouldRejectMissingName() throws Exception {
        mockMvc.perform(post("/villes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"population\":11000,\"codeDepartement\":\"67\"}"))
                .andExpect(status().isBadRequest());
        verify(villeService, never()).ajouterVille(any(VilleDto.class));
    }

    @Test
    void ajouterVille_shouldRejectNegativePopulation() throws Exception {
        mockMvc.perform(post("/villes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Saverne\",\"population\":-10,\"codeDepartement\":\"67\"}"))
                .andExpect(status().isBadRequest());
        verify(villeService, never()).ajouterVille(any(VilleDto.class));
    }

    @Test
    void ajouterVille_shouldRejectZeroPopulation() throws Exception {
        mockMvc.perform(post("/villes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Saverne\",\"population\":0,\"codeDepartement\":\"67\"}"))
                .andExpect(status().isBadRequest());
        verify(villeService, never()).ajouterVille(any(VilleDto.class));
    }

    @Test
    void modifierVille_shouldRejectInvalidData() throws Exception {
        mockMvc.perform(put("/villes/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"X\",\"population\":-5,\"codeDepartement\":\"67\"}"))
                .andExpect(status().isBadRequest());
        verify(villeService, never()).modifierVille(anyInt(), any(VilleDto.class));
    }

    @Test
    void getVille_shouldReturnExistingCity() throws Exception {
        when(villeService.getVille(1)).thenReturn(new VilleDto(1, "Strasbourg", 279284, "67", 39));
        mockMvc.perform(get("/villes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Strasbourg"))
                .andExpect(jsonPath("$.population").value(279284));
        verify(villeService).getVille(1);
    }
}