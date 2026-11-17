package com.example.plateforme_de_don.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.plateforme_de_don.dto.AnnonceInput;
import com.example.plateforme_de_don.dto.RechercheInput;
import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import com.example.plateforme_de_don.model.RechercheSauvegardee;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.UtilisateurRepository;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test du service de notification : les recherches sauvegardées (avec
 * notifications actives) correspondant à une nouvelle annonce déclenchent
 * un e-mail. JavaMailSender est simulé : aucun envoi réel.
 */
@SpringBootTest
@Transactional
class NotificationServiceTest {

    @Autowired
    AnnonceService annonces;

    @Autowired
    RechercheService recherches;

    @Autowired
    UtilisateurRepository utilisateurs;

    @Autowired
    PasswordEncoder encodeurMotsDePasse;

    @MockitoBean
    JavaMailSender envoiDeMails;

    private Utilisateur creeUtilisateur(String email, String pseudo) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse(encodeurMotsDePasse.encode("motdepasse"));
        utilisateur.setPseudo(pseudo);
        return utilisateurs.save(utilisateur);
    }

    @Test
    void uneAnnonceCorrespondanteDeclencheUnEmail() {
        Utilisateur bob = creeUtilisateur("bob@test.fr", "bob");
        Utilisateur alice = creeUtilisateur("alice@test.fr", "alice");

        // bob sauvegarde une recherche avec notifications actives
        RechercheInput criteres = new RechercheInput("Vélos à Paris", "Paris", "velo",
                null, null, true);
        RechercheSauvegardee recherche = recherches.cree(bob, criteres);

        // alice publie une annonce correspondante
        annonces.cree(alice, new AnnonceInput("Vélo de ville", "Très bon état",
                EtatObjet.BON_ETAT, ModeLivraison.ENVOI, "Paris", Set.of("velo")));

        verify(envoiDeMails).send(any(SimpleMailMessage.class));
    }

    @Test
    void uneAnnonceNonCorrespondanteNeDeclenchePasDEmail() {
        Utilisateur bob = creeUtilisateur("bob@test.fr", "bob");
        Utilisateur alice = creeUtilisateur("alice@test.fr", "alice");

        // recherche sur Paris uniquement
        RechercheInput criteres = new RechercheInput("Vélos à Paris", "Paris", "velo",
                null, null, true);
        recherches.cree(bob, criteres);

        // annonce publiée à Lyon : ne correspond pas
        annonces.cree(alice, new AnnonceInput("Vélo de ville", "Très bon état",
                EtatObjet.BON_ETAT, ModeLivraison.ENVOI, "Lyon", Set.of("velo")));

        verify(envoiDeMails, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void laRechercheDuDonneurLuiMemeNeDeclenchePasDEmail() {
        Utilisateur bob = creeUtilisateur("bob@test.fr", "bob");

        // bob sauvegarde une recherche puis publie lui-même l'annonce
        RechercheInput criteres = new RechercheInput("Vélos à Paris", "Paris", "velo",
                null, null, true);
        recherches.cree(bob, criteres);
        annonces.cree(bob, new AnnonceInput("Mon vélo", "Ma propre annonce",
                EtatObjet.BON_ETAT, ModeLivraison.ENVOI, "Paris", Set.of("velo")));

        verify(envoiDeMails, never()).send(any(SimpleMailMessage.class));
    }
}
