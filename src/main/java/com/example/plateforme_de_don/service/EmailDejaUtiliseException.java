package com.example.plateforme_de_don.service;

/**
 * Levée lors d'une inscription avec un email déjà pris (traduite en 409 par l'API).
 */
public class EmailDejaUtiliseException extends RuntimeException {

    public EmailDejaUtiliseException(String message) {
        super(message);
    }
}
