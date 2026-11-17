package com.example.plateforme_de_don.web;

import com.example.plateforme_de_don.dto.UtilisateurDto;
import com.example.plateforme_de_don.dto.UtilisateurInput;
import com.example.plateforme_de_don.mapper.UtilisateurRepresentation;
import com.example.plateforme_de_don.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ressource REST des utilisateurs : inscription et consultation de profil.
 */
@RestController
@RequestMapping("/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService service;
    private final UtilisateurRepresentation representation;

    public UtilisateurController(UtilisateurService service,
            UtilisateurRepresentation representation) {
        this.service = service;
        this.representation = representation;
    }

    @PostMapping
    public ResponseEntity<EntityModel<UtilisateurDto>> inscris(
            @Valid @RequestBody UtilisateurInput input) {
        EntityModel<UtilisateurDto> modele = representation.toModel(service.cree(input));
        return ResponseEntity.created(modele.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(modele);
    }

    @GetMapping("/{id}")
    public EntityModel<UtilisateurDto> detail(@PathVariable Long id) {
        return representation.toModel(service.parId(id));
    }
}
