package com.example.plateforme_de_don.repository;

import com.example.plateforme_de_don.model.MotCle;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MotCleRepository extends JpaRepository<MotCle, Long> {

    Optional<MotCle> findByLibelleIgnoreCase(String libelle);
}
