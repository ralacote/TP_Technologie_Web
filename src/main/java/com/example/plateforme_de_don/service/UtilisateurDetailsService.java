package com.example.plateforme_de_don.service;

import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Charge les utilisateurs depuis la base pour l'authentification HTTP Basic
 * (identifiant de connexion = email).
 */
@Service
public class UtilisateurDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurs;

    public UtilisateurDetailsService(UtilisateurRepository utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurs.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur " + email + " introuvable"));
        return User.withUsername(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .roles("UTILISATEUR")
                .build();
    }
}
