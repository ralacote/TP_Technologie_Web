package com.example.plateforme_de_don.repository;

import com.example.plateforme_de_don.model.Message;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Override
    @EntityGraph(attributePaths = {"annonce", "expediteur", "destinataire"})
    Optional<Message> findById(Long id);

    @EntityGraph(attributePaths = {"annonce", "expediteur", "destinataire"})
    List<Message> findByAnnonceIdOrderByDateEnvoiAsc(Long annonceId);

    @EntityGraph(attributePaths = {"annonce", "expediteur", "destinataire"})
    List<Message> findByDestinataireIdOrderByDateEnvoiDesc(Long destinataireId);

    @EntityGraph(attributePaths = {"annonce", "expediteur", "destinataire"})
    List<Message> findByExpediteurIdOrderByDateEnvoiDesc(Long expediteurId);
}
