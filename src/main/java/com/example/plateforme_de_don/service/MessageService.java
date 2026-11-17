package com.example.plateforme_de_don.service;

import com.example.plateforme_de_don.dto.MessageInput;
import com.example.plateforme_de_don.model.Annonce;
import com.example.plateforme_de_don.model.Message;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.AnnonceRepository;
import com.example.plateforme_de_don.repository.MessageRepository;
import com.example.plateforme_de_don.repository.UtilisateurRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MessageService {

    private final MessageRepository messages;
    private final AnnonceRepository annonces;
    private final UtilisateurRepository utilisateurs;

    public MessageService(MessageRepository messages, AnnonceRepository annonces,
            UtilisateurRepository utilisateurs) {
        this.messages = messages;
        this.annonces = annonces;
        this.utilisateurs = utilisateurs;
    }

    /** Envoie un message à propos d'une annonce. */
    public Message envoie(Utilisateur expediteur, MessageInput input) {
        Annonce annonce = annonces.findById(input.annonceId())
                .orElseThrow(() -> new NotFoundException(
                        "Annonce " + input.annonceId() + " introuvable"));
        Utilisateur destinataire = utilisateurs.findById(input.destinataireId())
                .orElseThrow(() -> new NotFoundException(
                        "Utilisateur " + input.destinataireId() + " introuvable"));
        Message message = new Message();
        message.setContenu(input.contenu());
        message.setAnnonce(annonce);
        message.setExpediteur(expediteur);
        message.setDestinataire(destinataire);
        return messages.save(message);
    }

    public List<Message> recus(Utilisateur utilisateur) {
        return messages.findByDestinataireIdOrderByDateEnvoiDesc(utilisateur.getId());
    }

    public List<Message> envoyes(Utilisateur utilisateur) {
        return messages.findByExpediteurIdOrderByDateEnvoiDesc(utilisateur.getId());
    }

    /** Conversation sur une annonce : visible par le donneur et par les
     *  participants des messages (expediteur/destinataire). */
    public List<Message> conversation(Utilisateur utilisateur, Long annonceId) {
        Annonce annonce = annonces.findById(annonceId)
                .orElseThrow(() -> new NotFoundException(
                        "Annonce " + annonceId + " introuvable"));
        return messages.findByAnnonceIdOrderByDateEnvoiAsc(annonceId).stream()
                .filter(message -> concerne(message, utilisateur, annonce))
                .toList();
    }

    private boolean concerne(Message message, Utilisateur utilisateur, Annonce annonce) {
        return message.getExpediteur().getId().equals(utilisateur.getId())
                || message.getDestinataire().getId().equals(utilisateur.getId())
                || annonce.getDonneur().getId().equals(utilisateur.getId());
    }

    /** Marque un message comme lu (réservé au destinataire). */
    public Message marqueLu(Utilisateur utilisateur, Long id) {
        Message message = messages.findById(id)
                .orElseThrow(() -> new NotFoundException("Message " + id + " introuvable"));
        if (!message.getDestinataire().getId().equals(utilisateur.getId())) {
            throw new NonProprietaireException(
                    "Seul le destinataire peut marquer le message comme lu");
        }
        message.setLu(true);
        return message;
    }
}
