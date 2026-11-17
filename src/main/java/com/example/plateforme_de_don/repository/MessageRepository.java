package com.example.plateforme_de_don.repository;

import com.example.plateforme_de_don.model.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByAnnonceIdOrderByDateEnvoiAsc(Long annonceId);

    List<Message> findByDestinataireIdOrderByDateEnvoiDesc(Long destinataireId);

    List<Message> findByExpediteurIdOrderByDateEnvoiDesc(Long expediteurId);
}
