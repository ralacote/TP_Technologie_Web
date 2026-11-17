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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Annonce de don d'objet : titre, description, état, date de publication,
 * zone géographique, mode de livraison et mots-clés.
 */
@Entity
@Table(name = "annonces")
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatObjet etat;

    @Column(nullable = false, updatable = false)
    private Instant datePublication;

    private String zoneGeographique;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModeLivraison modeLivraison;

    @ManyToMany
    @JoinTable(name = "annonce_mot_cle",
            joinColumns = @JoinColumn(name = "annonce_id"),
            inverseJoinColumns = @JoinColumn(name = "mot_cle_id"))
    private Set<MotCle> motCles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donneur_id")
    private Utilisateur donneur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private Lot lot;

    @PrePersist
    void onCreate() {
        if (datePublication == null) {
            datePublication = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EtatObjet getEtat() {
        return etat;
    }

    public void setEtat(EtatObjet etat) {
        this.etat = etat;
    }

    public Instant getDatePublication() {
        return datePublication;
    }

    public String getZoneGeographique() {
        return zoneGeographique;
    }

    public void setZoneGeographique(String zoneGeographique) {
        this.zoneGeographique = zoneGeographique;
    }

    public ModeLivraison getModeLivraison() {
        return modeLivraison;
    }

    public void setModeLivraison(ModeLivraison modeLivraison) {
        this.modeLivraison = modeLivraison;
    }

    public Set<MotCle> getMotCles() {
        return motCles;
    }

    public Utilisateur getDonneur() {
        return donneur;
    }

    public void setDonneur(Utilisateur donneur) {
        this.donneur = donneur;
    }

    public Lot getLot() {
        return lot;
    }

    public void setLot(Lot lot) {
        this.lot = lot;
    }

    public void addMotCle(MotCle motCle) {
        motCles.add(motCle);
    }

    public void removeMotCle(MotCle motCle) {
        motCles.remove(motCle);
    }
}
