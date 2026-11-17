package com.example.plateforme_de_don.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.plateforme_de_don.dto.AnnonceInput;
import com.example.plateforme_de_don.dto.LotInput;
import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.UtilisateurRepository;
import com.example.plateforme_de_don.service.AnnonceService;
import java.util.Set;
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
class LotControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UtilisateurRepository utilisateurs;

    @Autowired
    AnnonceService annonces;

    @Autowired
    PasswordEncoder encodeurMotsDePasse;

    private Utilisateur creeUtilisateur(String email, String pseudo) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse(encodeurMotsDePasse.encode("motdepasse"));
        utilisateur.setPseudo(pseudo);
        return utilisateurs.save(utilisateur);
    }

    private Long creeAnnonce(Utilisateur donneur, String titre) {
        return annonces.cree(donneur, new AnnonceInput(titre, "description",
                EtatObjet.BON_ETAT, ModeLivraison.MAIN_PROPRE, "Lyon", null)).getId();
    }

    @Test
    void cycleDeVieDUnLot() throws Exception {
        Utilisateur alice = creeUtilisateur("alice@test.fr", "alice");
        Long velo = creeAnnonce(alice, "Vélo");
        Long table = creeAnnonce(alice, "Table");

        // POST -> 201 avec les annonces regroupées
        MvcResult creation = mockMvc.perform(post("/lots")
                        .with(httpBasic("alice@test.fr", "motdepasse"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LotInput("Lot déménagement", Set.of(velo, table)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Lot déménagement"))
                .andExpect(jsonPath("$.proprietaire").value("alice"))
                .andExpect(jsonPath("$.annonces.length()").value(2))
                .andReturn();
        String location = creation.getResponse().getHeader("Location");
        Long lotId = Long.valueOf(location.substring(location.lastIndexOf('/') + 1));

        // GET liste puis détail
        mockMvc.perform(get("/lots").with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.lots[0].nom").value("Lot déménagement"));
        mockMvc.perform(get("/lots/{id}", lotId).with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annonces.length()").value(2));

        // DELETE -> 204 puis GET -> 404
        mockMvc.perform(delete("/lots/{id}", lotId).with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/lots/{id}", lotId).with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isNotFound());
    }

    @Test
    void lotAvecAnnonceDUnAutreUtilisateurRenvoie403() throws Exception {
        Utilisateur alice = creeUtilisateur("alice@test.fr", "alice");
        Utilisateur bob = creeUtilisateur("bob@test.fr", "bob");
        Long annonceDeBob = creeAnnonce(bob, "Vélo de bob");

        mockMvc.perform(post("/lots")
                        .with(httpBasic("alice@test.fr", "motdepasse"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LotInput("Lot piraté", Set.of(annonceDeBob)))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail")
                        .value("Un lot ne peut contenir que les annonces de son propriétaire"));
    }
}
