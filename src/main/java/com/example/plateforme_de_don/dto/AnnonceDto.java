package com.example.plateforme_de_don.dto;

import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.hateoas.server.core.Relation;

/**
 * Représentation d'une annonce exposée par l'API.
 */
@Relation(collectionRelation = "annonces", itemRelation = "annonce")
public record AnnonceDto(
        Long id,
        String titre,
        String description,
        EtatObjet etat,
        Instant datePublication,
        String zoneGeographique,
        ModeLivraison modeLivraison,
        Set<String> motCles,
        Long donneurId,
        String donneur) {

    public AnnonceDto {
        motCles = motCles == null ? Set.of() : Set.copyOf(motCles);
    }

    public static AnnonceDto of(Long id, String titre, String description, EtatObjet etat,
            Instant datePublication, String zoneGeographique, ModeLivraison modeLivraison,
            Set<String> motCles, Long donneurId, String donneur) {
        return new AnnonceDto(id, titre, description, etat, datePublication, zoneGeographique,
                modeLivraison, new LinkedHashSet<>(motCles), donneurId, donneur);
    }
}
