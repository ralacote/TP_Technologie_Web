package com.example.plateforme_de_don.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Données d'entrée pour l'envoi d'un message. */
public record MessageInput(
        @NotNull Long annonceId,
        @NotNull Long destinataireId,
        @NotBlank @Size(max = 2000) String contenu) {
}
