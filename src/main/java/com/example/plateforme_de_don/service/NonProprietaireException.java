package com.example.plateforme_de_don.service;

/**
 * Levée quand un utilisateur tente de modifier ou supprimer une ressource
 * qui ne lui appartient pas.
 */
public class NonProprietaireException extends RuntimeException {

    public NonProprietaireException(String message) {
        super(message);
    }
}
