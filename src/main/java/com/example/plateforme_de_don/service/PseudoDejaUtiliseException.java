package com.example.plateforme_de_don.service;

/** Levée quand un pseudo est déjà utilisé par un autre utilisateur. */
public class PseudoDejaUtiliseException extends RuntimeException {

    public PseudoDejaUtiliseException(String message) {
        super(message);
    }
}
