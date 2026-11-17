package com.example.plateforme_de_don.repository;

import com.example.plateforme_de_don.model.RechercheSauvegardee;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RechercheSauvegardeeRepository extends JpaRepository<RechercheSauvegardee, Long> {

    List<RechercheSauvegardee> findByProprietaireId(Long proprietaireId);

    List<RechercheSauvegardee> findByProprietaireIdAndNotificationsActivesTrue(Long proprietaireId);
}
