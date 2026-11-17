package com.example.plateforme_de_don.mapper;

import com.example.plateforme_de_don.dto.MessageDto;
import com.example.plateforme_de_don.model.Message;
import com.example.plateforme_de_don.web.AnnonceController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

/**
 * Construit la représentation hypermédia d'un message (lien vers l'annonce).
 */
@Component
public class MessageRepresentation
        implements RepresentationModelAssembler<Message, EntityModel<MessageDto>> {

    @Override
    public EntityModel<MessageDto> toModel(Message message) {
        MessageDto dto = new MessageDto(message.getId(), message.getContenu(),
                message.getDateEnvoi(), message.isLu(), message.getAnnonce().getId(),
                message.getAnnonce().getTitre(), message.getExpediteur().getPseudo(),
                message.getDestinataire().getPseudo());
        return EntityModel.of(dto, lienAnnonce(message.getAnnonce().getId()));
    }

    private Link lienAnnonce(Long annonceId) {
        return WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(AnnonceController.class).detail(annonceId))
                .withRel("annonce");
    }
}
