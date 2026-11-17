package com.example.plateforme_de_don.dto;

import jakarta.validation.constraints.NotNull;

/** Identifiant de l'annonce à mettre en favori. */
public record FavoriInput(@NotNull Long annonceId) {
}
