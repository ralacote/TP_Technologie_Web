package com.example.plateforme_de_don.service;

/**
 * Levée lorsqu'une ressource demandée n'existe pas (traduite en 404 par l'API).
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
