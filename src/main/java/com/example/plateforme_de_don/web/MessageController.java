package com.example.plateforme_de_don.web;

import com.example.plateforme_de_don.dto.MessageDto;
import com.example.plateforme_de_don.dto.MessageInput;
import com.example.plateforme_de_don.mapper.MessageRepresentation;
import com.example.plateforme_de_don.model.Message;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.service.MessageService;
import com.example.plateforme_de_don.service.UtilisateurService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ressource REST de la messagerie interne de l'utilisateur connecté :
 * messages échangés à propos d'une annonce pour mettre au point le don.
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService service;
    private final UtilisateurService utilisateurs;
    private final MessageRepresentation representation;

    public MessageController(MessageService service, UtilisateurService utilisateurs,
            MessageRepresentation representation) {
        this.service = service;
        this.utilisateurs = utilisateurs;
        this.representation = representation;
    }

    @PostMapping
    public ResponseEntity<EntityModel<MessageDto>> envoie(
            @AuthenticationPrincipal UserDetails utilisateurConnecte,
            @Valid @RequestBody MessageInput input) {
        Utilisateur expediteur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        Message message = service.envoie(expediteur, input);
        EntityModel<MessageDto> modele = representation.toModel(message);
        return ResponseEntity.created(URI.create("/messages/" + message.getId())).body(modele);
    }

    @GetMapping("/recus")
    public CollectionModel<EntityModel<MessageDto>> recus(
            @AuthenticationPrincipal UserDetails utilisateurConnecte) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        return CollectionModel.of(service.recus(utilisateur).stream()
                .map(representation::toModel).toList());
    }

    @GetMapping("/envoyes")
    public CollectionModel<EntityModel<MessageDto>> envoyes(
            @AuthenticationPrincipal UserDetails utilisateurConnecte) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        return CollectionModel.of(service.envoyes(utilisateur).stream()
                .map(representation::toModel).toList());
    }

    @GetMapping("/annonce/{annonceId}")
    public CollectionModel<EntityModel<MessageDto>> conversation(
            @AuthenticationPrincipal UserDetails utilisateurConnecte,
            @PathVariable Long annonceId) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        return CollectionModel.of(service.conversation(utilisateur, annonceId).stream()
                .map(representation::toModel).toList());
    }

    @PutMapping("/{id}/lu")
    public EntityModel<MessageDto> marqueLu(
            @AuthenticationPrincipal UserDetails utilisateurConnecte, @PathVariable Long id) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        return representation.toModel(service.marqueLu(utilisateur, id));
    }
}
