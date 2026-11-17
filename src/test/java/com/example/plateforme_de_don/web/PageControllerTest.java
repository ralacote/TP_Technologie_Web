package com.example.plateforme_de_don.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests de l'interface Web simple (HTML, rendu côte serveur) : les pages
 * publiques et les formulaires (inscription, publication, favori, message).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PageControllerTest {

    @Autowired
    MockMvc mockMvc;

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
                EtatObjet.BON_ETAT, ModeLivraison.ENVOI, "Paris", null)).getId();
    }

    @Test
    void accueilEtDetailAnnonceSontPublics() throws Exception {
        Utilisateur alice = creeUtilisateur("alice@test.fr", "alice");
        Long id = creeAnnonce(alice, "Vélo de ville");

        // l'accueil liste les annonces
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Vélo de ville")));

        // la page de détail affiche les informations de l'annonce
        mockMvc.perform(get("/annonce/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers
                        .containsString("description")));
    }

    @Test
    void inscriptionDepuisLeFormulaireWeb() throws Exception {
        // le formulaire est public
        mockMvc.perform(get("/inscription"))
                .andExpect(status().isOk());

        // la soumission crée l'utilisateur et redirige vers l'accueil
        mockMvc.perform(post("/inscription")
                        .param("email", "web@test.fr")
                        .param("pseudo", "web")
                        .param("motDePasse", "motdepasse"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // un email déjà utilisé redirige vers le formulaire avec une erreur
        mockMvc.perform(post("/inscription")
                        .param("email", "web@test.fr")
                        .param("pseudo", "web2")
                        .param("motDePasse", "motdepasse"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inscription?erreur=email"));
    }

    @Test
    void publicationFavoriEtMessageDepuisLesPages() throws Exception {
        Utilisateur alice = creeUtilisateur("alice@test.fr", "alice");
        Utilisateur bob = creeUtilisateur("bob@test.fr", "bob");

        // publication d'une annonce via le formulaire web
        mockMvc.perform(post("/publier").with(httpBasic("bob@test.fr", "motdepasse"))
                        .param("titre", "Table basse")
                        .param("description", "Bon état")
                        .param("etat", "BON_ETAT")
                        .param("modeLivraison", "MAIN_PROPRE")
                        .param("zoneGeographique", "Lyon")
                        .param("motCles", "table, salon"))
                .andExpect(status().is3xxRedirection());

        Long id = annonces.liste(org.springframework.data.domain.PageRequest.of(0, 10))
                .filter(annonce -> "Table basse".equals(annonce.getTitre())).toList()
                .get(0).getId();

        // mise en favori depuis la page de détail
        mockMvc.perform(post("/annonce/{id}/favori", id)
                        .with(httpBasic("bob@test.fr", "motdepasse")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/annonce/" + id));

        // la page "mes favoris" affiche l'annonce
        mockMvc.perform(get("/mes-favoris").with(httpBasic("bob@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Table basse")));

        // message au donneur depuis la page de détail
        mockMvc.perform(post("/annonce/{id}/message", id)
                        .with(httpBasic("alice@test.fr", "motdepasse"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("contenu", "Bonjour, je suis interesse"))
                .andExpect(status().is3xxRedirection());

        // la page "mes messages" liste les échanges
        mockMvc.perform(get("/mes-messages").with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers
                        .containsString("Bonjour, je suis interesse")));
    }

    @Test
    void pagesPersonnellesExigentUneAuthentification() throws Exception {
        mockMvc.perform(get("/mes-favoris"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/mes-lots"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/mes-messages"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/publier"))
                .andExpect(status().isUnauthorized());
    }
}
