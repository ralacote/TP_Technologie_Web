package com.example.plateforme_de_don.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

/** Données d'entrée pour la création d'un lot. */
public record LotInput(
        @NotBlank @Size(max = 200) String nom,
        @NotEmpty Set<Long> annonceIds) {
}
