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
import com.example.plateforme_de_don.service.AnnonceService;
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

    @Autowired
    AnnonceService annonces;

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

    private Long creeAnnonce(Utilisateur donneur, String titre, String description,
            EtatObjet etat, ModeLivraison mode, String zone, Set<String> motCles) {
        return annonces.cree(new AnnonceInput(titre, description, etat, mode, zone, motCles,
                donneur.getId())).getId();
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

    @Test
    void filtreParZoneInsensibleALaCasse() throws Exception {
        Utilisateur donneur = donneur();
        creeAnnonce(donneur, "Canapé", "Canapé trois places", EtatObjet.BON_ETAT,
                ModeLivraison.MAIN_PROPRE, "Clermont-Ferrand", Set.of("maison"));
        creeAnnonce(donneur, "Commode", "Commode ancienne", EtatObjet.SATISFAISANT,
                ModeLivraison.ENVOI, "Lyon", Set.of("maison"));

        mockMvc.perform(get("/annonces").param("zone", "clermont"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$._embedded.annonces[0].titre").value("Canapé"));
    }

    @Test
    void filtreParMotCle() throws Exception {
        Utilisateur donneur = donneur();
        creeAnnonce(donneur, "Ballon", "Ballon de football", EtatObjet.BON_ETAT,
                ModeLivraison.ENVOI, "Paris", Set.of("sport"));
        creeAnnonce(donneur, "Roman", "Roman policier", EtatObjet.SATISFAISANT,
                ModeLivraison.ENVOI, "Paris", Set.of("livre"));

        mockMvc.perform(get("/annonces").param("motCle", "sport"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$._embedded.annonces[0].titre").value("Ballon"));
    }

    @Test
    void filtreParEtat() throws Exception {
        Utilisateur donneur = donneur();
        creeAnnonce(donneur, "Téléphone", "Encore sous garantie", EtatObjet.NEUF,
                ModeLivraison.ENVOI, "Paris", null);
        creeAnnonce(donneur, "Chargeur", "Câble un peu abîmé", EtatObjet.ENDOMMAGE,
                ModeLivraison.ENVOI, "Paris", null);

        mockMvc.perform(get("/annonces").param("etat", "NEUF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$._embedded.annonces[0].titre").value("Téléphone"));
    }

    @Test
    void filtreParModeInclutLesAnnoncesLesDeux() throws Exception {
        Utilisateur donneur = donneur();
        creeAnnonce(donneur, "Chaise", "À récupérer sur place", EtatObjet.BON_ETAT,
                ModeLivraison.MAIN_PROPRE, "Lyon", null);
        creeAnnonce(donneur, "Lampe", "Peut être postée", EtatObjet.BON_ETAT,
                ModeLivraison.ENVOI, "Lyon", null);
        creeAnnonce(donneur, "Vaisselle", "Au choix", EtatObjet.BON_ETAT,
                ModeLivraison.LES_DEUX, "Lyon", null);

        // « main propre » inclut les annonces acceptant les deux modes
        mockMvc.perform(get("/annonces").param("mode", "MAIN_PROPRE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2));

        mockMvc.perform(get("/annonces").param("mode", "LES_DEUX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void rechercheTextuelleSurTitreEtDescription() throws Exception {
        Utilisateur donneur = donneur();
        creeAnnonce(donneur, "Vélo enfant", "Petit vélo 16 pouces", EtatObjet.BON_ETAT,
                ModeLivraison.ENVOI, "Paris", null);
        creeAnnonce(donneur, "Casque", "Pour protections diverses", EtatObjet.SATISFAISANT,
                ModeLivraison.ENVOI, "Paris", null);

        // « vélo » est dans le titre de l'une et la description de l'autre ? non :
        // ici un seul résultat attendu (titre), le casque ne contient pas « vélo »
        mockMvc.perform(get("/annonces").param("q", "vélo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$._embedded.annonces[0].titre").value("Vélo enfant"));

        mockMvc.perform(get("/annonces").param("q", "protections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$._embedded.annonces[0].titre").value("Casque"));
    }

    @Test
    void filtresCombinables() throws Exception {
        Utilisateur donneur = donneur();
        creeAnnonce(donneur, "Canapé", "Canapé neuf jamais servi", EtatObjet.NEUF,
                ModeLivraison.MAIN_PROPRE, "Clermont-Ferrand", Set.of("maison"));
        creeAnnonce(donneur, "Canapé d'angle", "Quelques taches", EtatObjet.ENDOMMAGE,
                ModeLivraison.MAIN_PROPRE, "Clermont-Ferrand", Set.of("maison"));
        creeAnnonce(donneur, "Table", "Table neuve", EtatObjet.NEUF,
                ModeLivraison.MAIN_PROPRE, "Lyon", Set.of("maison"));

        mockMvc.perform(get("/annonces")
                        .param("zone", "clermont").param("etat", "NEUF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$._embedded.annonces[0].titre").value("Canapé"));
    }

    @Test
    void etatInvalideRenvoie400() throws Exception {
        mockMvc.perform(get("/annonces").param("etat", "CASSE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Paramètre invalide : etat"));
    }
}
