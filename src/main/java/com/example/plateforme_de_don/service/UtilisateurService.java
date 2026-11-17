package com.example.plateforme_de_don.service;

import com.example.plateforme_de_don.dto.UtilisateurInput;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurs;

    public UtilisateurService(UtilisateurRepository utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    /** Inscription d'une personne utilisatrice. */
    public Utilisateur cree(UtilisateurInput input) {
        if (utilisateurs.existsByEmail(input.email())) {
            throw new EmailDejaUtiliseException("Email déjà utilisé : " + input.email());
        }
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(input.email());
        utilisateur.setPseudo(input.pseudo());
        // TODO : encoder le mot de passe (BCrypt) à l'étape authentification
        utilisateur.setMotDePasse(input.motDePasse());
        return utilisateurs.save(utilisateur);
    }

    public Utilisateur parId(Long id) {
        return utilisateurs.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur " + id + " introuvable"));
    }
}
