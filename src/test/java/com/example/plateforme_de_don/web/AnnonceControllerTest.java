package com.example.plateforme_de_don.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.plateforme_de_don.dto.AnnonceInput;
import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.UtilisateurRepository;
import tools.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnnonceControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UtilisateurRepository utilisateurs;

    private Utilisateur donneur() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("alice@test.fr");
        utilisateur.setMotDePasse("secret");
        utilisateur.setPseudo("alice");
        return utilisateurs.save(utilisateur);
    }

    private AnnonceInput inputValide(Utilisateur donneur) {
        return new AnnonceInput("Vélo de ville", "Vélo en bon état, peu servi",
                EtatObjet.BON_ETAT, ModeLivraison.LES_DEUX, "Clermont-Ferrand",
                Set.of("vélo", "sport"), donneur.getId());
    }

    @Test
    void cycleDeVieCompletDUneAnnonce() throws Exception {
        Utilisateur donneur = donneur();

        // POST -> 201 + Location + liens HAL
        MvcResult creation = mockMvc.perform(post("/annonces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputValide(donneur))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titre").value("Vélo de ville"))
                .andExpect(jsonPath("$.donneur").value("alice"))
                .andExpect(jsonPath("$.motCles.length()").value(2))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andReturn();
        String location = creation.getResponse().getHeader("Location");
        Long id = Long.valueOf(location.substring(location.lastIndexOf('/') + 1));

        // GET collection -> 200, annonce présente dans _embedded.annonces
        mockMvc.perform(get("/annonces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.annonces[0].titre").value("Vélo de ville"))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        // GET detail -> 200
        mockMvc.perform(get("/annonces/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Vélo en bon état, peu servi"));

        // PUT -> 200 avec modifications
        AnnonceInput modifiee = new AnnonceInput("Vélo de ville électrique", "Assistant ajouté",
                EtatObjet.TRES_BON_ETAT, ModeLivraison.MAIN_PROPRE, "Clermont-Ferrand",
                Set.of("vélo"), donneur.getId());
        mockMvc.perform(put("/annonces/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifiee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Vélo de ville électrique"))
                .andExpect(jsonPath("$.etat").value("TRES_BON_ETAT"))
                .andExpect(jsonPath("$.motCles.length()").value(1));

        // DELETE -> 204 puis GET -> 404
        mockMvc.perform(delete("/annonces/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/annonces/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void donneesInvalidesRenvoientUnProbleme400() throws Exception {
        Utilisateur donneur = donneur();
        AnnonceInput invalide = new AnnonceInput("  ", "description", EtatObjet.BON_ETAT,
                ModeLivraison.ENVOI, null, null, donneur.getId());

        mockMvc.perform(post("/annonces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalide)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.erreurs").isArray());
    }

    @Test
    void annonceInconnueRenvoie404() throws Exception {
        mockMvc.perform(get("/annonces/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Annonce 9999 introuvable"));
    }

    @Test
    void donneurInconnuRenvoie404ALaCreation() throws Exception {
        AnnonceInput sansDonneur = new AnnonceInput("Table", "Table basse", EtatObjet.SATISFAISANT,
                ModeLivraison.MAIN_PROPRE, "Lyon", null, 9999L);

        mockMvc.perform(post("/annonces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sansDonneur)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Utilisateur 9999 introuvable"));
    }
}
