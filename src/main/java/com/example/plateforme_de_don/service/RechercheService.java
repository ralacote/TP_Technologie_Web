package com.example.plateforme_de_don.service;

import com.example.plateforme_de_don.dto.RechercheInput;
import com.example.plateforme_de_don.model.RechercheSauvegardee;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.RechercheSauvegardeeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RechercheService {

    private final RechercheSauvegardeeRepository recherches;

    public RechercheService(RechercheSauvegardeeRepository recherches) {
        this.recherches = recherches;
    }

    /** Sauvegarde une recherche pour l'utilisateur connecté. */
    public RechercheSauvegardee cree(Utilisateur proprietaire, RechercheInput input) {
        RechercheSauvegardee recherche = new RechercheSauvegardee();
        applique(recherche, input);
        recherche.setProprietaire(proprietaire);
        return recherches.save(recherche);
    }

    public List<RechercheSauvegardee> liste(Utilisateur proprietaire) {
        return recherches.findByProprietaireId(proprietaire.getId());
    }

    public RechercheSauvegardee parId(Utilisateur utilisateur, Long id) {
        RechercheSauvegardee recherche = recherches.findById(id)
                .orElseThrow(() -> new NotFoundException("Recherche " + id + " introuvable"));
        if (!recherche.getProprietaire().getId().equals(utilisateur.getId())) {
            throw new NonProprietaireException(
                    "Seul le propriétaire de la recherche peut effectuer cette opération");
        }
        return recherche;
    }

    public void supprime(Utilisateur utilisateur, Long id) {
        recherches.delete(parId(utilisateur, id));
    }

    private void applique(RechercheSauvegardee recherche, RechercheInput input) {
        recherche.setNom(input.nom());
        recherche.setZoneGeographique(input.zoneGeographique());
        recherche.setMotCle(input.motCle());
        recherche.setEtat(input.etat());
        recherche.setModeLivraison(input.modeLivraison());
        recherche.setNotificationsActives(
                input.notificationsActives() == null || input.notificationsActives());
    }
}
