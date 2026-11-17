package com.example.plateforme_de_don.dto;

import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import jakarta.validation.constraints.Size;

/** Données d'entrée pour la création d'une recherche sauvegardée. */
public record RechercheInput(
        @Size(max = 200) String nom,
        @Size(max = 200) String zoneGeographique,
        @Size(max = 100) String motCle,
        EtatObjet etat,
        ModeLivraison modeLivraison,
        Boolean notificationsActives) {
}
