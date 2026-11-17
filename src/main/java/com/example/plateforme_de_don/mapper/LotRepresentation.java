package com.example.plateforme_de_don.mapper;

import com.example.plateforme_de_don.dto.AnnonceDto;
import com.example.plateforme_de_don.dto.LotDto;
import com.example.plateforme_de_don.model.Lot;
import com.example.plateforme_de_don.web.LotController;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

/**
 * Construit la représentation hypermédia d'un lot (avec les annonces regroupées).
 */
@Component
public class LotRepresentation
        implements RepresentationModelAssembler<Lot, EntityModel<LotDto>> {

    private final AnnonceRepresentation annonces;

    public LotRepresentation(AnnonceRepresentation annonces) {
        this.annonces = annonces;
    }

    @Override
    public EntityModel<LotDto> toModel(Lot lot) {
        Set<AnnonceDto> annoncesDuLot = new LinkedHashSet<>(lot.getAnnonces().stream()
                .map(annonce -> annonces.toModel(annonce).getContent()).toList());
        LotDto dto = new LotDto(lot.getId(), lot.getNom(), lot.getDateCreation(),
                lot.getProprietaire().getPseudo(), annoncesDuLot);
        return EntityModel.of(dto, lienSelf(lot.getId()));
    }

    private Link lienSelf(Long id) {
        return WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(LotController.class).detail(null, id)).withSelfRel();
    }
}
