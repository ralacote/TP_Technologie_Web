package com.example.plateforme_de_don.repository;

import com.example.plateforme_de_don.model.Lot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LotRepository extends JpaRepository<Lot, Long> {

    List<Lot> findByProprietaireId(Long proprietaireId);
}
