package com.example.plateforme_de_don.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.plateforme_de_don.dto.UtilisateurInput;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.UtilisateurRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UtilisateurControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder encodeurMotsDePasse;

    @Autowired
    UtilisateurRepository utilisateurs;

    @Test
    void inscriptionPuisConsultationDuProfil() throws Exception {
        UtilisateurInput input = new UtilisateurInput("bob@test.fr", "bob", "motdepasse");

        MvcResult creation = mockMvc.perform(post("/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("bob@test.fr"))
                .andExpect(jsonPath("$.pseudo").value("bob"))
                .andExpect(jsonPath("$.dateInscription").exists())
                // le mot de passe ne doit jamais apparaître dans la représentation
                .andExpect(jsonPath("$.motDePasse").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andReturn();

        String location = creation.getResponse().getHeader("Location");
        Long id = Long.valueOf(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(get("/utilisateurs/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pseudo").value("bob"));
    }

    @Test
    void emailEnDoubleRenvoie409() throws Exception {
        UtilisateurInput input = new UtilisateurInput("carol@test.fr", "carol", "motdepasse");
        mockMvc.perform(post("/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Email déjà utilisé : carol@test.fr"));
    }

    @Test
    void donneesInvalidesRenvoientUnProbleme400() throws Exception {
        UtilisateurInput invalide = new UtilisateurInput("pas-un-email", "bob", "court");

        mockMvc.perform(post("/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalide)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs").isArray());
    }

    @Test
    void utilisateurInconnuRenvoie404() throws Exception {
        mockMvc.perform(get("/utilisateurs/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Utilisateur 9999 introuvable"));
    }

    @Test
    void motDePasseStockeEstEncodeAvecBCrypt() throws Exception {
        UtilisateurInput input = new UtilisateurInput("dave@test.fr", "dave", "motdepasse");
        mockMvc.perform(post("/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated());

        Utilisateur utilisateur = utilisateurs.findByEmail("dave@test.fr").orElseThrow();
        // le mot de passe en clair n'est jamais stocké
        assertThat(utilisateur.getMotDePasse()).isNotEqualTo("motdepasse");
        // il est vérifiable par l'encodeur BCrypt
        assertThat(encodeurMotsDePasse.matches("motdepasse", utilisateur.getMotDePasse())).isTrue();
    }

    @Test
    void connexionAvecIdentifiantsValidesOuInvalides() throws Exception {
        UtilisateurInput input = new UtilisateurInput("eve@test.fr", "eve", "motdepasse");
        MvcResult creation = mockMvc.perform(post("/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andReturn();
        String location = creation.getResponse().getHeader("Location");
        Long id = Long.valueOf(location.substring(location.lastIndexOf('/') + 1));

        // identifiants corrects -> consultation OK
        mockMvc.perform(get("/utilisateurs/{id}", id)
                        .with(httpBasic("eve@test.fr", "motdepasse")))
                .andExpect(status().isOk());

        // mauvais mot de passe -> 401
        mockMvc.perform(get("/utilisateurs/{id}", id)
                        .with(httpBasic("eve@test.fr", "mauvais")))
                .andExpect(status().isUnauthorized());

        // utilisateur inconnu -> 401
        mockMvc.perform(get("/utilisateurs/{id}", id)
                        .with(httpBasic("inconnu@test.fr", "motdepasse")))
                .andExpect(status().isUnauthorized());
    }
}
