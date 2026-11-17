package com.example.plateforme_de_don.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données d'entrée pour l'inscription d'un utilisateur.
 */
public record UtilisateurInput(
        @NotBlank @Email @Size(max = 200) String email,
        @NotBlank @Size(max = 100) String pseudo,
        @NotBlank @Size(min = 8, max = 100) String motDePasse) {
}
