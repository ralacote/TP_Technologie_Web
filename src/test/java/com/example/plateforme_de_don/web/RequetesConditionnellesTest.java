package com.example.plateforme_de_don.web;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.plateforme_de_don.dto.AnnonceInput;
import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.UtilisateurRepository;
import com.example.plateforme_de_don.service.AnnonceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests du cache HTTP (passage a l'echelle) : ETag / If-None-Match -> 304
 * Not Modified, en-tete Cache-Control, et nouvel ETag quand le contenu evolue.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RequetesConditionnellesTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UtilisateurRepository utilisateurs;

    @Autowired
    AnnonceService annonces;

    @Autowired
    PasswordEncoder encodeurMotsDePasse;

    private Long creeAnnonce(String titre) {
        Utilisateur donneur = new Utilisateur();
        donneur.setEmail("cache-donneur@test.fr");
        donneur.setMotDePasse(encodeurMotsDePasse.encode("motdepasse"));
        donneur.setPseudo("cachedonneur");
        donneur = utilisateurs.save(donneur);
        return annonces.cree(donneur, new AnnonceInput(titre, "description",
                EtatObjet.BON_ETAT, ModeLivraison.ENVOI, "Paris", null)).getId();
    }

    @Test
    void etagRenvoie304QuandRienNaChange() throws Exception {
        // premiere consultation : 200 avec un ETag et l'en-tete de cache
        String etag = mockMvc.perform(get("/annonces"))
                .andExpect(status().isOk())
                .andExpect(header().exists("ETag"))
                .andExpect(header().string("Cache-Control", "no-cache"))
                .andReturn().getResponse().getHeader("ETag");

        // deuxieme consultation avec If-None-Match : rien n'a change -> 304
        mockMvc.perform(get("/annonces").header("If-None-Match", etag))
                .andExpect(status().isNotModified())
                .andExpect(content().string(""));
    }

    @Test
    void etagChangeQuandLeContenuChange() throws Exception {
        String etagAvant = mockMvc.perform(get("/annonces"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader("ETag");

        // une nouvelle annonce modifie la liste -> nouvel ETag, 200 complet
        creeAnnonce("Plante verte");
        mockMvc.perform(get("/annonces").header("If-None-Match", etagAvant))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", not(equalTo(etagAvant))));
    }

    @Test
    void ressourcesStatiquesMisesEnCache() throws Exception {
        // le CSS est stocke une heure par le navigateur (application.properties)
        mockMvc.perform(get("/css/style.css"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=3600"));
    }
}
