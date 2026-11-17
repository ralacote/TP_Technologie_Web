package com.example.plateforme_de_don.web;

import com.example.plateforme_de_don.dto.AnnonceDto;
import com.example.plateforme_de_don.dto.FavoriInput;
import com.example.plateforme_de_don.mapper.AnnonceRepresentation;
import com.example.plateforme_de_don.model.Favori;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.service.FavoriService;
import com.example.plateforme_de_don.service.UtilisateurService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ressource REST des favoris de l'utilisateur connecté : les annonces
 * favorites sont exposées via leur représentation habituelle.
 */
@RestController
@RequestMapping("/favoris")
public class FavoriController {

    private final FavoriService service;
    private final UtilisateurService utilisateurs;
    private final AnnonceRepresentation representation;

    public FavoriController(FavoriService service, UtilisateurService utilisateurs,
            AnnonceRepresentation representation) {
        this.service = service;
        this.utilisateurs = utilisateurs;
        this.representation = representation;
    }

    @PostMapping
    public ResponseEntity<EntityModel<AnnonceDto>> ajoute(
            @AuthenticationPrincipal UserDetails utilisateurConnecte,
            @Valid @RequestBody FavoriInput input) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        Favori favori = service.ajoute(utilisateur, input.annonceId());
        EntityModel<AnnonceDto> modele = representation.toModel(favori.getAnnonce());
        return ResponseEntity.created(URI.create("/favoris/" + favori.getAnnonce().getId()))
                .body(modele);
    }

    @GetMapping
    public CollectionModel<EntityModel<AnnonceDto>> liste(
            @AuthenticationPrincipal UserDetails utilisateurConnecte) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        return CollectionModel.of(service.liste(utilisateur).stream()
                .map(favori -> representation.toModel(favori.getAnnonce()))
                .toList());
    }

    @DeleteMapping("/{annonceId}")
    public ResponseEntity<Void> supprime(
            @AuthenticationPrincipal UserDetails utilisateurConnecte,
            @PathVariable Long annonceId) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        service.supprime(utilisateur, annonceId);
        return ResponseEntity.noContent().build();
    }
}
