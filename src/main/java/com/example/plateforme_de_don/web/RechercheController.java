package com.example.plateforme_de_don.web;

import com.example.plateforme_de_don.dto.AnnonceDto;
import com.example.plateforme_de_don.dto.RechercheDto;
import com.example.plateforme_de_don.dto.RechercheInput;
import com.example.plateforme_de_don.mapper.AnnonceRepresentation;
import com.example.plateforme_de_don.mapper.RechercheRepresentation;
import com.example.plateforme_de_don.model.Annonce;
import com.example.plateforme_de_don.model.RechercheSauvegardee;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.service.AnnonceService;
import com.example.plateforme_de_don.service.RechercheService;
import com.example.plateforme_de_don.service.UtilisateurService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
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
 * Ressource REST des recherches sauvegardées de l'utilisateur connecté :
 * réservées aux personnes enregistrées, avec exécution des critères.
 */
@RestController
@RequestMapping("/recherches")
public class RechercheController {

    private final RechercheService service;
    private final AnnonceService annonceService;
    private final UtilisateurService utilisateurs;
    private final RechercheRepresentation representation;
    private final AnnonceRepresentation annonceRepresentation;

    public RechercheController(RechercheService service, AnnonceService annonceService,
            UtilisateurService utilisateurs, RechercheRepresentation representation,
            AnnonceRepresentation annonceRepresentation) {
        this.service = service;
        this.annonceService = annonceService;
        this.utilisateurs = utilisateurs;
        this.representation = representation;
        this.annonceRepresentation = annonceRepresentation;
    }

    @PostMapping
    public ResponseEntity<EntityModel<RechercheDto>> cree(
            @AuthenticationPrincipal UserDetails utilisateurConnecte,
            @Valid @RequestBody RechercheInput input) {
        Utilisateur proprietaire = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        RechercheSauvegardee recherche = service.cree(proprietaire, input);
        EntityModel<RechercheDto> modele = representation.toModel(recherche);
        return ResponseEntity.created(URI.create("/recherches/" + recherche.getId()))
                .body(modele);
    }

    @GetMapping
    public CollectionModel<EntityModel<RechercheDto>> liste(
            @AuthenticationPrincipal UserDetails utilisateurConnecte) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        return CollectionModel.of(service.liste(utilisateur).stream()
                .map(representation::toModel).toList());
    }

    @GetMapping("/{id}")
    public EntityModel<RechercheDto> detail(
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

    /** Exécute la recherche sauvegardée : retourne les annonces correspondantes. */
    @GetMapping("/{id}/resultats")
    public PagedModel<EntityModel<AnnonceDto>> resultats(
            @AuthenticationPrincipal UserDetails utilisateurConnecte, @PathVariable Long id,
            @PageableDefault(size = 10) Pageable pageable,
            PagedResourcesAssembler<Annonce> assembler) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        RechercheSauvegardee recherche = service.parId(utilisateur, id);
        Page<Annonce> page = annonceService.recherche(recherche.getZoneGeographique(),
                recherche.getMotCle(), recherche.getEtat(), recherche.getModeLivraison(), null,
                pageable);
        return assembler.toModel(page, annonceRepresentation);
    }
}
