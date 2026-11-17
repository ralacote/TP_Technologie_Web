package com.example.plateforme_de_don.service;

/** Levée quand une annonce est déjà dans les favoris de l'utilisateur. */
public class FavoriDejaExistantException extends RuntimeException {

    public FavoriDejaExistantException(String message) {
        super(message);
    }
}
