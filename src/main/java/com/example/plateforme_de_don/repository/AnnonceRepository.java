package com.example.plateforme_de_don.repository;

import com.example.plateforme_de_don.model.Annonce;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Accès aux annonces. Les associations consultées par les représentations
 * (donneur, mots-clés) sont chargées avec la requête : avec open-in-view
 * désactivé, elles doivent être initialisées pendant la transaction.
 */
public interface AnnonceRepository extends JpaRepository<Annonce, Long>,
        JpaSpecificationExecutor<Annonce> {

    @Override
    @EntityGraph(attributePaths = {"donneur", "motCles"})
    Optional<Annonce> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"donneur", "motCles"})
    Page<Annonce> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"donneur", "motCles"})
    Page<Annonce> findAll(Specification<Annonce> specification, Pageable pageable);

    List<Annonce> findByDonneurId(Long donneurId);

    List<Annonce> findByLotId(Long lotId);
}
