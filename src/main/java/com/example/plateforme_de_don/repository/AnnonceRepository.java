package com.example.plateforme_de_don.repository;

import com.example.plateforme_de_don.model.Annonce;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnonceRepository extends JpaRepository<Annonce, Long> {

    List<Annonce> findByDonneurId(Long donneurId);

    List<Annonce> findByLotId(Long lotId);
}
