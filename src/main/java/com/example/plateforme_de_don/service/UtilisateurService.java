package com.example.plateforme_de_don.service;

import com.example.plateforme_de_don.dto.UtilisateurInput;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder encodeurMotsDePasse;

    public UtilisateurService(UtilisateurRepository utilisateurs,
            PasswordEncoder encodeurMotsDePasse) {
        this.utilisateurs = utilisateurs;
        this.encodeurMotsDePasse = encodeurMotsDePasse;
    }

    /** Inscription d'une personne utilisatrice (mot de passe encodé en BCrypt). */
    public Utilisateur cree(UtilisateurInput input) {
        if (utilisateurs.existsByEmail(input.email())) {
            throw new EmailDejaUtiliseException("Email déjà utilisé : " + input.email());
        }
        if (utilisateurs.existsByPseudo(input.pseudo())) {
            throw new PseudoDejaUtiliseException("Pseudo déjà utilisé : " + input.pseudo());
        }
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(input.email());
        utilisateur.setPseudo(input.pseudo());
        utilisateur.setMotDePasse(encodeurMotsDePasse.encode(input.motDePasse()));
        return utilisateurs.save(utilisateur);
    }

    public Utilisateur parId(Long id) {
        return utilisateurs.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur " + id + " introuvable"));
    }

    /** Recherche par email (identifiant de connexion). */
    public Utilisateur parEmail(String email) {
        return utilisateurs.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur " + email + " introuvable"));
    }
}
