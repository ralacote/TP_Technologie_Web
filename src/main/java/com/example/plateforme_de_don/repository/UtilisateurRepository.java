package com.example.plateforme_de_don.repository;

import com.example.plateforme_de_don.model.Utilisateur;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findByPseudo(String pseudo);

    boolean existsByEmail(String email);

    boolean existsByPseudo(String pseudo);
}
