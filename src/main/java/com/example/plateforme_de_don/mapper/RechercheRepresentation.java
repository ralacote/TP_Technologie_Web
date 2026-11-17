package com.example.plateforme_de_don.mapper;

import com.example.plateforme_de_don.dto.RechercheDto;
import com.example.plateforme_de_don.model.RechercheSauvegardee;
import com.example.plateforme_de_don.web.RechercheController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

/**
 * Construit la représentation hypermédia d'une recherche sauvegardée
 * (liens self et résultats).
 */
@Component
public class RechercheRepresentation
        implements RepresentationModelAssembler<RechercheSauvegardee, EntityModel<RechercheDto>> {

    @Override
    public EntityModel<RechercheDto> toModel(RechercheSauvegardee recherche) {
        RechercheDto dto = new RechercheDto(recherche.getId(), recherche.getNom(),
                recherche.getZoneGeographique(), recherche.getMotCle(), recherche.getEtat(),
                recherche.getModeLivraison(), recherche.isNotificationsActives(),
                recherche.getDateCreation());
        return EntityModel.of(dto, lienSelf(recherche.getId()),
                lienResultats(recherche.getId()));
    }

    private Link lienSelf(Long id) {
        return WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(RechercheController.class).detail(null, id))
                .withSelfRel();
    }

    private Link lienResultats(Long id) {
        return WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(RechercheController.class).resultats(null, id, null,
                        null))
                .withRel("resultats");
    }
}
