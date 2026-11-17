package com.example.plateforme_de_don.service;

import com.example.plateforme_de_don.model.Annonce;
import com.example.plateforme_de_don.model.Favori;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.AnnonceRepository;
import com.example.plateforme_de_don.repository.FavoriRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FavoriService {

    private final FavoriRepository favoris;
    private final AnnonceRepository annonces;

    public FavoriService(FavoriRepository favoris, AnnonceRepository annonces) {
        this.favoris = favoris;
        this.annonces = annonces;
    }

    /** Ajoute une annonce aux favoris de l'utilisateur connecté. */
    public Favori ajoute(Utilisateur utilisateur, Long annonceId) {
        if (favoris.existsByUtilisateurIdAndAnnonceId(utilisateur.getId(), annonceId)) {
            throw new FavoriDejaExistantException(
                    "Annonce " + annonceId + " déjà dans les favoris");
        }
        Annonce annonce = annonces.findById(annonceId)
                .orElseThrow(() -> new NotFoundException("Annonce " + annonceId + " introuvable"));
        Favori favori = new Favori();
        favori.setUtilisateur(utilisateur);
        favori.setAnnonce(annonce);
        return favoris.save(favori);
    }

    public List<Favori> liste(Utilisateur utilisateur) {
        return favoris.findByUtilisateurId(utilisateur.getId());
    }

    public void supprime(Utilisateur utilisateur, Long annonceId) {
        Favori favori = favoris.findByUtilisateurIdAndAnnonceId(utilisateur.getId(), annonceId)
                .orElseThrow(() -> new NotFoundException(
                        "Annonce " + annonceId + " absente des favoris"));
        favoris.delete(favori);
    }
}
