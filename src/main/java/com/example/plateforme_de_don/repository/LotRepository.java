package com.example.plateforme_de_don.repository;

import com.example.plateforme_de_don.model.Lot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LotRepository extends JpaRepository<Lot, Long> {

    @Override
    @EntityGraph(attributePaths = {"proprietaire", "annonces", "annonces.donneur",
            "annonces.motCles"})
    Optional<Lot> findById(Long id);

    @EntityGraph(attributePaths = {"proprietaire", "annonces", "annonces.donneur",
            "annonces.motCles"})
    List<Lot> findByProprietaireId(Long proprietaireId);
}
