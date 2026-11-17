package com.example.plateforme_de_don.mapper;

import com.example.plateforme_de_don.dto.AnnonceDto;
import com.example.plateforme_de_don.model.Annonce;
import com.example.plateforme_de_don.model.MotCle;
import com.example.plateforme_de_don.web.AnnonceController;
import java.util.LinkedHashSet;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

/**
 * Construit la représentation hypermédia d'une annonce (liens self et collection).
 */
@Component
public class AnnonceRepresentation implements RepresentationModelAssembler<Annonce, EntityModel<AnnonceDto>> {

    @Override
    public EntityModel<AnnonceDto> toModel(Annonce annonce) {
        AnnonceDto dto = new AnnonceDto(
                annonce.getId(),
                annonce.getTitre(),
                annonce.getDescription(),
                annonce.getEtat(),
                annonce.getDatePublication(),
                annonce.getZoneGeographique(),
                annonce.getModeLivraison(),
                new LinkedHashSet<>(annonce.getMotCles().stream().map(MotCle::getLibelle).toList()),
                annonce.getDonneur().getId(),
                annonce.getDonneur().getPseudo());
        return EntityModel.of(dto, liens(annonce));
    }

    private Link[] liens(Annonce annonce) {
        return new Link[] {
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(AnnonceController.class).detail(annonce.getId()))
                        .withSelfRel(),
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(AnnonceController.class).liste(null, null))
                        .withRel(IanaLinkRelations.COLLECTION)
        };
    }
}
