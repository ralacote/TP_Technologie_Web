package com.example.plateforme_de_don.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Lot d'objets d'une même personne, pour une transaction groupée.
 */
@Entity
@Table(name = "lots")
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proprietaire_id")
    private Utilisateur proprietaire;

    @OneToMany(mappedBy = "lot")
    private Set<Annonce> annonces = new HashSet<>();

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

    public Instant getDateCreation() {
        return dateCreation;
    }

    public Utilisateur getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(Utilisateur proprietaire) {
        this.proprietaire = proprietaire;
    }

    public Set<Annonce> getAnnonces() {
        return annonces;
    }

    public void addAnnonce(Annonce annonce) {
        annonces.add(annonce);
        annonce.setLot(this);
    }

    public void removeAnnonce(Annonce annonce) {
        annonces.remove(annonce);
        annonce.setLot(null);
    }
}
