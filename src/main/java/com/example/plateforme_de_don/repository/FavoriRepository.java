package com.example.plateforme_de_don.repository;

import com.example.plateforme_de_don.model.Favori;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriRepository extends JpaRepository<Favori, Long> {

    List<Favori> findByUtilisateurId(Long utilisateurId);

    Optional<Favori> findByUtilisateurIdAndAnnonceId(Long utilisateurId, Long annonceId);

    boolean existsByUtilisateurIdAndAnnonceId(Long utilisateurId, Long annonceId);
}
