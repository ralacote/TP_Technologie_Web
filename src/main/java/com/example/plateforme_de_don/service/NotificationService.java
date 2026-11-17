package com.example.plateforme_de_don.service;

import com.example.plateforme_de_don.model.Annonce;
import com.example.plateforme_de_don.model.RechercheSauvegardee;
import com.example.plateforme_de_don.repository.RechercheSauvegardeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Notifie par e-mail les utilisateurs dont une recherche sauvegardée (avec
 * notifications actives) correspond à une nouvelle annonce. L'envoi peut être
 * désactivé globalement (app.notifications.email-active) et par recherche.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender envoiDeMails;
    private final RechercheSauvegardeeRepository recherches;
    private final boolean emailsActifs;

    public NotificationService(JavaMailSender envoiDeMails,
            RechercheSauvegardeeRepository recherches,
            @Value("${app.notifications.email-active:true}") boolean emailsActifs) {
        this.envoiDeMails = envoiDeMails;
        this.recherches = recherches;
        this.emailsActifs = emailsActifs;
    }

    /** Appelé à la création d'une annonce : notifie les recherches correspondantes. */
    public void notifieNouvelleAnnonce(Annonce annonce) {
        if (!emailsActifs) {
            return;
        }
        for (RechercheSauvegardee recherche : recherches.findByNotificationsActivesTrue()) {
            boolean dUnAutreUtilisateur =
                    !recherche.getProprietaire().getId().equals(annonce.getDonneur().getId());
            if (dUnAutreUtilisateur && recherche.correspondA(annonce)) {
                envoieEmail(recherche, annonce);
            }
        }
    }

    private void envoieEmail(RechercheSauvegardee recherche, Annonce annonce) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(recherche.getProprietaire().getEmail());
            mail.setSubject("Nouvelle annonce correspondant à votre recherche « "
                    + (recherche.getNom() == null ? recherche.getId() : recherche.getNom()) + " »");
            mail.setText("Une nouvelle annonce correspond à votre recherche sauvegardée :\n\n"
                    + annonce.getTitre() + " (" + annonce.getEtat() + ")\n"
                    + (annonce.getZoneGeographique() == null ? "" : "Zone : "
                            + annonce.getZoneGeographique() + "\n")
                    + "\n" + annonce.getDescription());
            envoiDeMails.send(mail);
        } catch (Exception e) {
            // la notification ne doit jamais bloquer la création de l'annonce
            log.warn("Notification non envoyée à {} : {}", recherche.getProprietaire().getEmail(),
                    e.getMessage());
        }
    }
}
