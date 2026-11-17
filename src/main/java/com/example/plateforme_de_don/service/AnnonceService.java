package com.example.plateforme_de_don.service;

import com.example.plateforme_de_don.dto.AnnonceInput;
import com.example.plateforme_de_don.model.Annonce;
import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import com.example.plateforme_de_don.model.MotCle;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.AnnonceRepository;
import com.example.plateforme_de_don.repository.AnnonceSpecifications;
import com.example.plateforme_de_don.repository.MotCleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class AnnonceService {

    private final AnnonceRepository annonces;
    private final MotCleRepository motCles;
    private final NotificationService notifications;

    public AnnonceService(AnnonceRepository annonces, MotCleRepository motCles,
            NotificationService notifications) {
        this.annonces = annonces;
        this.motCles = motCles;
        this.notifications = notifications;
    }

    public Page<Annonce> liste(Pageable pageable) {
        return annonces.findAll(pageable);
    }

    /** Recherche d'annonces avec filtres combinables (zone, mot-clé, état, mode, texte). */
    public Page<Annonce> recherche(String zone, String motCle, EtatObjet etat,
            ModeLivraison mode, String texte, Pageable pageable) {
        List<Specification<Annonce>> criteres = new ArrayList<>();
        if (StringUtils.hasText(zone)) {
            criteres.add(AnnonceSpecifications.dansZone(zone));
        }
        if (StringUtils.hasText(motCle)) {
            criteres.add(AnnonceSpecifications.avecMotCle(motCle));
        }
        if (etat != null) {
            criteres.add(AnnonceSpecifications.avecEtat(etat));
        }
        if (mode != null) {
            criteres.add(AnnonceSpecifications.livrablePar(mode));
        }
        if (StringUtils.hasText(texte)) {
            criteres.add(AnnonceSpecifications.contientTexte(texte));
        }
        if (criteres.isEmpty()) {
            return annonces.findAll(pageable);
        }
        return annonces.findAll(Specification.allOf(criteres), pageable);
    }

    public Annonce parId(Long id) {
        return annonces.findById(id)
                .orElseThrow(() -> new NotFoundException("Annonce " + id + " introuvable"));
    }

    public Annonce cree(Utilisateur donneur, AnnonceInput input) {
        Annonce annonce = new Annonce();
        applique(annonce, input);
        annonce.setDonneur(donneur);
        Annonce enregistree = annonces.save(annonce);
        // notifie les recherches sauvegardées correspondantes (aspect désactivable)
        notifications.notifieNouvelleAnnonce(enregistree);
        return enregistree;
    }

    public Annonce modifie(Long id, Utilisateur utilisateur, AnnonceInput input) {
        Annonce annonce = parId(id);
        verifieProprietaire(annonce, utilisateur);
        applique(annonce, input);
        return annonces.save(annonce);
    }

    public void supprime(Long id, Utilisateur utilisateur) {
        Annonce annonce = parId(id);
        verifieProprietaire(annonce, utilisateur);
        annonces.delete(annonce);
    }

    /** Seul le donneur peut modifier ou supprimer son annonce. */
    private void verifieProprietaire(Annonce annonce, Utilisateur utilisateur) {
        if (!annonce.getDonneur().getId().equals(utilisateur.getId())) {
            throw new NonProprietaireException(
                    "Seul le donneur de l'annonce peut effectuer cette opération");
        }
    }

    private void applique(Annonce annonce, AnnonceInput input) {
        annonce.setTitre(input.titre());
        annonce.setDescription(input.description());
        annonce.setEtat(input.etat());
        annonce.setModeLivraison(input.modeLivraison());
        annonce.setZoneGeographique(input.zoneGeographique());
        annonce.getMotCles().clear();
        if (input.motCles() != null) {
            input.motCles().forEach(libelle -> annonce.addMotCle(existanteOuNouvelle(libelle)));
        }
    }

    private MotCle existanteOuNouvelle(String libelle) {
        String normalise = libelle.trim().toLowerCase(Locale.ROOT);
        return motCles.findByLibelleIgnoreCase(normalise).orElseGet(() -> {
            MotCle motCle = new MotCle();
            motCle.setLibelle(normalise);
            return motCles.save(motCle);
        });
    }
}
