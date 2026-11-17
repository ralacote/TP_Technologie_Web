package com.example.plateforme_de_don.service;

import com.example.plateforme_de_don.dto.LotInput;
import com.example.plateforme_de_don.model.Annonce;
import com.example.plateforme_de_don.model.Lot;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.AnnonceRepository;
import com.example.plateforme_de_don.repository.LotRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LotService {

    private final LotRepository lots;
    private final AnnonceRepository annonces;

    public LotService(LotRepository lots, AnnonceRepository annonces) {
        this.lots = lots;
        this.annonces = annonces;
    }

    /** Crée un lot regroupant des annonces du même donneur (le propriétaire). */
    public Lot cree(Utilisateur proprietaire, LotInput input) {
        Lot lot = new Lot();
        lot.setNom(input.nom());
        lot.setProprietaire(proprietaire);
        for (Long annonceId : input.annonceIds()) {
            lot.addAnnonce(annonceDuProprietaire(proprietaire, annonceId));
        }
        return lots.save(lot);
    }

    public List<Lot> liste(Utilisateur proprietaire) {
        return lots.findByProprietaireId(proprietaire.getId());
    }

    public Lot parId(Utilisateur utilisateur, Long id) {
        Lot lot = lots.findById(id)
                .orElseThrow(() -> new NotFoundException("Lot " + id + " introuvable"));
        if (!lot.getProprietaire().getId().equals(utilisateur.getId())) {
            throw new NonProprietaireException(
                    "Seul le propriétaire du lot peut effectuer cette opération");
        }
        return lot;
    }

    public void supprime(Utilisateur utilisateur, Long id) {
        Lot lot = parId(utilisateur, id);
        // détache les annonces du lot avant sa suppression
        lot.getAnnonces().forEach(annonce -> annonce.setLot(null));
        lots.delete(lot);
    }

    /** L'annonce doit exister et appartenir au propriétaire du lot. */
    private Annonce annonceDuProprietaire(Utilisateur proprietaire, Long annonceId) {
        Annonce annonce = annonces.findById(annonceId)
                .orElseThrow(() -> new NotFoundException("Annonce " + annonceId + " introuvable"));
        if (!annonce.getDonneur().getId().equals(proprietaire.getId())) {
            throw new NonProprietaireException(
                    "Un lot ne peut contenir que les annonces de son propriétaire");
        }
        return annonce;
    }
}
