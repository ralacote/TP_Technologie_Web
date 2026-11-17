package com.example.plateforme_de_don.dto;

import java.time.Instant;
import org.springframework.hateoas.server.core.Relation;

/**
 * Représentation d'un utilisateur exposée par l'API (jamais le mot de passe).
 */
@Relation(collectionRelation = "utilisateurs", itemRelation = "utilisateur")
public record UtilisateurDto(
        Long id,
        String email,
        String pseudo,
        Instant dateInscription) {
}
