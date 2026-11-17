package com.example.plateforme_de_don.dto;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.hateoas.server.core.Relation;

/** Représentation d'un lot exposée par l'API. */
@Relation(collectionRelation = "lots", itemRelation = "lot")
public record LotDto(
        Long id,
        String nom,
        Instant dateCreation,
        String proprietaire,
        Set<AnnonceDto> annonces) {

    public LotDto {
        annonces = annonces == null ? Set.of() : Set.copyOf(annonces);
    }
}
