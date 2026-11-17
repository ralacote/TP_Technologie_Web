package com.example.plateforme_de_don.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.plateforme_de_don.dto.AnnonceInput;
import com.example.plateforme_de_don.dto.FavoriInput;
import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.UtilisateurRepository;
import com.example.plateforme_de_don.service.AnnonceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FavoriControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    tools.jackson.databind.ObjectMapper mapper;

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
                EtatObjet.BON_ETAT, ModeLivraison.ENVOI, "Paris", Set.of("velo"))).getId();
    }

    @Test
    void cycleDeVieDUnFavori() throws Exception {
        Utilisateur alice = creeUtilisateur("alice@test.fr", "alice");
        Long annonceId = creeAnnonce(alice, "Vélo");

        // POST -> 201
        mockMvc.perform(post("/favoris")
                        .with(httpBasic("alice@test.fr", "motdepasse"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new FavoriInput(annonceId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titre").value("Vélo"));

        // GET -> l'annonce est dans les favoris
        mockMvc.perform(get("/favoris").with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.annonces[0].titre").value("Vélo"));

        // doublon -> 409
        mockMvc.perform(post("/favoris")
                        .with(httpBasic("alice@test.fr", "motdepasse"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new FavoriInput(annonceId))))
                .andExpect(status().isConflict());

        // DELETE -> 204 puis liste vide
        mockMvc.perform(delete("/favoris/{annonceId}", annonceId)
                        .with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/favoris").with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    void ajoutSansAuthentificationRenvoie401() throws Exception {
        Utilisateur alice = creeUtilisateur("alice@test.fr", "alice");
        Long annonceId = creeAnnonce(alice, "Vélo");

        mockMvc.perform(post("/favoris")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new FavoriInput(annonceId))))
                .andExpect(status().isUnauthorized());
    }
}
