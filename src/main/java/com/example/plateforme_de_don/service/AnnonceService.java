package com.example.plateforme_de_don.service;

import com.example.plateforme_de_don.dto.AnnonceInput;
import com.example.plateforme_de_don.model.Annonce;
import com.example.plateforme_de_don.model.MotCle;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.AnnonceRepository;
import com.example.plateforme_de_don.repository.MotCleRepository;
import com.example.plateforme_de_don.repository.UtilisateurRepository;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnnonceService {

    private final AnnonceRepository annonces;
    private final UtilisateurRepository utilisateurs;
    private final MotCleRepository motCles;

    public AnnonceService(AnnonceRepository annonces, UtilisateurRepository utilisateurs,
            MotCleRepository motCles) {
        this.annonces = annonces;
        this.utilisateurs = utilisateurs;
        this.motCles = motCles;
    }

    public Page<Annonce> liste(Pageable pageable) {
        return annonces.findAll(pageable);
    }

    public Annonce parId(Long id) {
        return annonces.findById(id)
                .orElseThrow(() -> new NotFoundException("Annonce " + id + " introuvable"));
    }

    public Annonce cree(AnnonceInput input) {
        Utilisateur donneur = utilisateurs.findById(input.donneurId())
                .orElseThrow(() -> new NotFoundException(
                        "Utilisateur " + input.donneurId() + " introuvable"));
        Annonce annonce = new Annonce();
        applique(annonce, input);
        annonce.setDonneur(donneur);
        return annonces.save(annonce);
    }

    public Annonce modifie(Long id, AnnonceInput input) {
        Annonce annonce = parId(id);
        applique(annonce, input);
        return annonces.save(annonce);
    }

    public void supprime(Long id) {
        annonces.delete(parId(id));
    }

    private void applique(Annonce annonce, AnnonceInput input) {
        annonce.setTitre(input.titre());
        annonce.setDescription(input.description());
        annonce.setEtat(input.etat());
        annonce.setModeLivraison(input.modeLivraison());
        annonce.setZoneGeographique(input.zoneGeographique());
        annonce.getMotCles().clear();
        if (input.motCles() != null) {
            input.motCles().forEach(libelle -> annonce.addMotCle(existanteOuNouvelle(libelle)));
        }
    }

    private MotCle existanteOuNouvelle(String libelle) {
        String normalise = libelle.trim().toLowerCase(Locale.ROOT);
        return motCles.findByLibelleIgnoreCase(normalise).orElseGet(() -> {
            MotCle motCle = new MotCle();
            motCle.setLibelle(normalise);
            return motCles.save(motCle);
        });
    }
}
