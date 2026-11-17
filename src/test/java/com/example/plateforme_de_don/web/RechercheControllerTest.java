package com.example.plateforme_de_don.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.plateforme_de_don.dto.RechercheInput;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RechercheControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UtilisateurRepository utilisateurs;

    @Autowired
    PasswordEncoder encodeurMotsDePasse;

    private Utilisateur creeUtilisateur(String email, String pseudo) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse(encodeurMotsDePasse.encode("motdepasse"));
        utilisateur.setPseudo(pseudo);
        return utilisateurs.save(utilisateur);
    }

    @Test
    void cycleDeVieDUneRechercheSauvegardee() throws Exception {
        creeUtilisateur("alice@test.fr", "alice");

        // POST -> 201
        MvcResult creation = mockMvc.perform(post("/recherches")
                        .with(httpBasic("alice@test.fr", "motdepasse"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RechercheInput(
                                "Vélos à Lyon", "Lyon", "vélo", null, null, true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Vélos à Lyon"))
                .andExpect(jsonPath("$.zoneGeographique").value("Lyon"))
                .andExpect(jsonPath("$.notificationsActives").value(true))
                .andExpect(jsonPath("$._links.resultats.href").exists())
                .andReturn();
        String location = creation.getResponse().getHeader("Location");
        Long id = Long.valueOf(location.substring(location.lastIndexOf('/') + 1));

        // GET liste puis détail
        mockMvc.perform(get("/recherches").with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.recherches[0].nom").value("Vélos à Lyon"));
        mockMvc.perform(get("/recherches/{id}", id)
                        .with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isOk());

        // GET resultats -> 200 (aucune annonce, page vide)
        mockMvc.perform(get("/recherches/{id}/resultats", id)
                        .with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(0));

        // DELETE -> 204 puis GET -> 404
        mockMvc.perform(delete("/recherches/{id}", id)
                        .with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/recherches/{id}", id)
                        .with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isNotFound());
    }

    @Test
    void consultationParUnAutreUtilisateurRenvoie403() throws Exception {
        Utilisateur alice = creeUtilisateur("alice@test.fr", "alice");
        creeUtilisateur("bob@test.fr", "bob");

        MvcResult creation = mockMvc.perform(post("/recherches")
                        .with(httpBasic("alice@test.fr", "motdepasse"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RechercheInput("Ma recherche", "Paris", null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn();
        String location = creation.getResponse().getHeader("Location");
        Long id = Long.valueOf(location.substring(location.lastIndexOf('/') + 1));

        // bob ne peut pas consulter la recherche d'alice
        mockMvc.perform(get("/recherches/{id}", id).with(httpBasic("bob@test.fr", "motdepasse")))
                .andExpect(status().isForbidden());

        // ni ses résultats
        mockMvc.perform(get("/recherches/{id}/resultats", id)
                        .with(httpBasic("bob@test.fr", "motdepasse")))
                .andExpect(status().isForbidden());
    }
}
