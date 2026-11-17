package com.example.plateforme_de_don.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;

/**
 * Recherche sauvegardée par un utilisateur enregistré, avec ses critères de
 * filtrage. Une notification est envoyée lors de l'apparition de nouvelles
 * annonces correspondantes, si activée.
 */
@Entity
@Table(name = "recherches_sauvegardees")
public class RechercheSauvegardee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String zoneGeographique;

    private String motCle;

    @Enumerated(EnumType.STRING)
    private EtatObjet etat;

    @Enumerated(EnumType.STRING)
    private ModeLivraison modeLivraison;

    @Column(nullable = false)
    private boolean notificationsActives = true;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proprietaire_id")
    private Utilisateur proprietaire;

    /** Correspond aux critères de la recherche sauvegardée ? */
    public boolean correspondA(Annonce annonce) {
        if (zoneGeographique != null && !zoneGeographique.isBlank()) {
            if (annonce.getZoneGeographique() == null
                    || !annonce.getZoneGeographique().toLowerCase(Locale.ROOT)
                            .contains(zoneGeographique.trim().toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        if (motCle != null && !motCle.isBlank()) {
            String cible = motCle.trim().toLowerCase(Locale.ROOT);
            if (annonce.getMotCles().stream().noneMatch(
                    motCleAnnonce -> motCleAnnonce.getLibelle().equals(cible))) {
                return false;
            }
        }
        if (etat != null && annonce.getEtat() != etat) {
            return false;
        }
        if (modeLivraison != null && !livrableSelon(annonce.getModeLivraison())) {
            return false;
        }
        return true;
    }

    /** « main propre » et « envoi » incluent aussi les annonces acceptant les deux. */
    private boolean livrableSelon(ModeLivraison modeDeLAnnonce) {
        if (modeLivraison == ModeLivraison.LES_DEUX) {
            return modeDeLAnnonce == ModeLivraison.LES_DEUX;
        }
        return modeDeLAnnonce == modeLivraison || modeDeLAnnonce == ModeLivraison.LES_DEUX;
    }

    @PrePersist
    void onCreate() {
        if (dateCreation == null) {
            dateCreation = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getZoneGeographique() {
        return zoneGeographique;
    }

    public void setZoneGeographique(String zoneGeographique) {
        this.zoneGeographique = zoneGeographique;
    }

    public String getMotCle() {
        return motCle;
    }

    public void setMotCle(String motCle) {
        this.motCle = motCle;
    }

    public EtatObjet getEtat() {
        return etat;
    }

    public void setEtat(EtatObjet etat) {
        this.etat = etat;
    }

    public ModeLivraison getModeLivraison() {
        return modeLivraison;
    }

    public void setModeLivraison(ModeLivraison modeLivraison) {
        this.modeLivraison = modeLivraison;
    }

    public boolean isNotificationsActives() {
        return notificationsActives;
    }

    public void setNotificationsActives(boolean notificationsActives) {
        this.notificationsActives = notificationsActives;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public Utilisateur getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(Utilisateur proprietaire) {
        this.proprietaire = proprietaire;
    }
}
