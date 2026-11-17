package com.example.plateforme_de_don.mapper;

import com.example.plateforme_de_don.dto.UtilisateurDto;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.web.UtilisateurController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

/**
 * Construit la représentation hypermédia d'un utilisateur (lien self).
 */
@Component
public class UtilisateurRepresentation
        implements RepresentationModelAssembler<Utilisateur, EntityModel<UtilisateurDto>> {

    @Override
    public EntityModel<UtilisateurDto> toModel(Utilisateur utilisateur) {
        UtilisateurDto dto = new UtilisateurDto(utilisateur.getId(), utilisateur.getEmail(),
                utilisateur.getPseudo(), utilisateur.getDateInscription());
        return EntityModel.of(dto, lienSelf(utilisateur.getId()));
    }

    private Link lienSelf(Long id) {
        return WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(UtilisateurController.class).detail(id)).withSelfRel();
    }
}
