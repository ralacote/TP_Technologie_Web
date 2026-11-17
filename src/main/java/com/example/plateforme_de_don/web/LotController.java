package com.example.plateforme_de_don.web;

import com.example.plateforme_de_don.dto.LotDto;
import com.example.plateforme_de_don.dto.LotInput;
import com.example.plateforme_de_don.mapper.LotRepresentation;
import com.example.plateforme_de_don.model.Lot;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.service.LotService;
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
 * Ressource REST des lots de l'utilisateur connecté : regroupement d'annonces
 * du même donneur pour une transaction groupée.
 */
@RestController
@RequestMapping("/lots")
public class LotController {

    private final LotService service;
    private final UtilisateurService utilisateurs;
    private final LotRepresentation representation;

    public LotController(LotService service, UtilisateurService utilisateurs,
            LotRepresentation representation) {
        this.service = service;
        this.utilisateurs = utilisateurs;
        this.representation = representation;
    }

    @PostMapping
    public ResponseEntity<EntityModel<LotDto>> cree(
            @AuthenticationPrincipal UserDetails utilisateurConnecte,
            @Valid @RequestBody LotInput input) {
        Utilisateur proprietaire = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        Lot lot = service.cree(proprietaire, input);
        EntityModel<LotDto> modele = representation.toModel(lot);
        return ResponseEntity.created(URI.create("/lots/" + lot.getId())).body(modele);
    }

    @GetMapping
    public CollectionModel<EntityModel<LotDto>> liste(
            @AuthenticationPrincipal UserDetails utilisateurConnecte) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        return CollectionModel.of(service.liste(utilisateur).stream()
                .map(representation::toModel).toList());
    }

    @GetMapping("/{id}")
    public EntityModel<LotDto> detail(
            @AuthenticationPrincipal UserDetails utilisateurConnecte, @PathVariable Long id) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        return representation.toModel(service.parId(utilisateur, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprime(
            @AuthenticationPrincipal UserDetails utilisateurConnecte, @PathVariable Long id) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        service.supprime(utilisateur, id);
        return ResponseEntity.noContent().build();
    }
}
