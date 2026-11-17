package com.example.plateforme_de_don.dto;

import java.time.Instant;
import org.springframework.hateoas.server.core.Relation;

/** Représentation d'un message exposée par l'API. */
@Relation(collectionRelation = "messages", itemRelation = "message")
public record MessageDto(
        Long id,
        String contenu,
        Instant dateEnvoi,
        boolean lu,
        Long annonceId,
        String titreAnnonce,
        String expediteur,
        String destinataire) {
}
