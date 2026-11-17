package com.example.plateforme_de_don.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.plateforme_de_don.dto.AnnonceInput;
import com.example.plateforme_de_don.dto.MessageInput;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MessageControllerTest {

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

    private Long creeAnnonce(Utilisateur donneur) {
        return annonces.cree(donneur, new AnnonceInput("Vélo", "Vélo de ville",
                EtatObjet.BON_ETAT, ModeLivraison.MAIN_PROPRE, "Lyon", null)).getId();
    }

    private Long aliceEnvoieUnMessage(Utilisateur alice, Utilisateur bob, Long annonceId)
            throws Exception {
        MvcResult envoi = mockMvc.perform(post("/messages")
                        .with(httpBasic("bob@test.fr", "motdepasse"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MessageInput(annonceId,
                                alice.getId(), "Bonjour, je suis intéressé par le vélo"))))
                .andExpect(status().isCreated())
                .andReturn();
        String location = envoi.getResponse().getHeader("Location");
        return Long.valueOf(location.substring(location.lastIndexOf('/') + 1));
    }

    @Test
    void envoieConsultationEtMarquageLu() throws Exception {
        Utilisateur alice = creeUtilisateur("alice@test.fr", "alice");
        Utilisateur bob = creeUtilisateur("bob@test.fr", "bob");
        Long annonceId = creeAnnonce(alice);

        // bob envoie un message à alice à propos de l'annonce d'alice
        Long messageId = aliceEnvoieUnMessage(alice, bob, annonceId);

        // alice reçoit le message, non lu
        mockMvc.perform(get("/messages/recus").with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.messages[0].contenu")
                        .value("Bonjour, je suis intéressé par le vélo"))
                .andExpect(jsonPath("$._embedded.messages[0].lu").value(false))
                .andExpect(jsonPath("$._embedded.messages[0].expediteur").value("bob"));

        // bob le retrouve dans ses messages envoyés
        mockMvc.perform(get("/messages/envoyes").with(httpBasic("bob@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.messages[0].destinataire").value("alice"));

        // alice marque le message comme lu
        mockMvc.perform(put("/messages/{id}/lu", messageId)
                        .with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lu").value(true));

        // bob (non-destinataire) ne peut pas marquer le message comme lu
        mockMvc.perform(put("/messages/{id}/lu", messageId)
                        .with(httpBasic("bob@test.fr", "motdepasse")))
                .andExpect(status().isForbidden());
    }

    @Test
    void conversationSurUneAnnonceVisibleParLeDonneurEtLesParticipants() throws Exception {
        Utilisateur alice = creeUtilisateur("alice@test.fr", "alice");
        Utilisateur bob = creeUtilisateur("bob@test.fr", "bob");
        Utilisateur carol = creeUtilisateur("carol@test.fr", "carol");
        Long annonceId = creeAnnonce(alice);

        aliceEnvoieUnMessage(alice, bob, annonceId);

        // le donneur (alice) voit la conversation
        mockMvc.perform(get("/messages/annonce/{annonceId}", annonceId)
                        .with(httpBasic("alice@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.messages.length()").value(1));

        // bob (participant) voit la conversation
        mockMvc.perform(get("/messages/annonce/{annonceId}", annonceId)
                        .with(httpBasic("bob@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.messages.length()").value(1));

        // carol (tiers) ne voit aucun message
        mockMvc.perform(get("/messages/annonce/{annonceId}", annonceId)
                        .with(httpBasic("carol@test.fr", "motdepasse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }
}
