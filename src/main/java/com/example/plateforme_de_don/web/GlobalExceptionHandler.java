package com.example.plateforme_de_don.web;

import com.example.plateforme_de_don.service.EmailDejaUtiliseException;
import com.example.plateforme_de_don.service.FavoriDejaExistantException;
import com.example.plateforme_de_don.service.NonProprietaireException;
import com.example.plateforme_de_don.service.NotFoundException;
import com.example.plateforme_de_don.service.PseudoDejaUtiliseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduit les exceptions en réponses Problem Details (RFC 7807).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail introuvable(NotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(EmailDejaUtiliseException.class)
    ProblemDetail emailDejaUtilise(EmailDejaUtiliseException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(FavoriDejaExistantException.class)
    ProblemDetail favoriDejaExistant(FavoriDejaExistantException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(PseudoDejaUtiliseException.class)
    ProblemDetail pseudoDejaUtilise(PseudoDejaUtiliseException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(NonProprietaireException.class)
    ProblemDetail nonProprietaire(NonProprietaireException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException exception) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Données invalides");
        probleme.setProperty("erreurs", exception.getBindingResult().getFieldErrors().stream()
                .map(erreur -> erreur.getField() + " : " + erreur.getDefaultMessage())
                .toList());
        return probleme;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail illisible(HttpMessageNotReadableException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Corps de requête illisible ou JSON invalide");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail parametreInvalide(MethodArgumentTypeMismatchException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Paramètre invalide : " + exception.getName());
    }
}
