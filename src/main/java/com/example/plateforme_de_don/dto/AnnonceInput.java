package com.example.plateforme_de_don.dto;

import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * Données d'entrée pour la création ou la modification d'une annonce.
 * Le donneur est l'utilisateur authentifié, jamais transmis par le client.
 */
public record AnnonceInput(
        @NotBlank @Size(max = 200) String titre,
        @NotBlank @Size(max = 2000) String description,
        @NotNull EtatObjet etat,
        @NotNull ModeLivraison modeLivraison,
        String zoneGeographique,
        Set<String> motCles) {
}
