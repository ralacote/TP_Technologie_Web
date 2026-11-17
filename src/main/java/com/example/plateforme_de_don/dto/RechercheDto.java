package com.example.plateforme_de_don.dto;

import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import java.time.Instant;
import org.springframework.hateoas.server.core.Relation;

/** Représentation d'une recherche sauvegardée exposée par l'API. */
@Relation(collectionRelation = "recherches", itemRelation = "recherche")
public record RechercheDto(
        Long id,
        String nom,
        String zoneGeographique,
        String motCle,
        EtatObjet etat,
        ModeLivraison modeLivraison,
        boolean notificationsActives,
        Instant dateCreation) {
}
