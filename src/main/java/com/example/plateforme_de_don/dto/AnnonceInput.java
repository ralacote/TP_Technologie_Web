package com.example.plateforme_de_don.dto;

import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * Données d'entrée pour la création ou la modification d'une annonce.
 */
public record AnnonceInput(
        @NotBlank @Size(max = 200) String titre,
        @NotBlank @Size(max = 2000) String description,
        @NotNull EtatObjet etat,
        @NotNull ModeLivraison modeLivraison,
        String zoneGeographique,
        Set<String> motCles,
        // Temporaire : sera remplacé par l'utilisateur authentifié à l'étape sécurité
        Long donneurId) {
}
