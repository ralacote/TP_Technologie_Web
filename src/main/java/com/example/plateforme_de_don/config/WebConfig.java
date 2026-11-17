package com.example.plateforme_de_don.config;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cache HTTP (passage a l'echelle du sujet) : requetes conditionnelles (ETag /
 * If-None-Match -> 304 Not Modified) et en-tetes de cache sur les reponses.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * ETag calcule sur le corps des reponses GET/HEAD : si le client renvoie
     * If-None-Match avec le meme ETag, il recoit 304 Not Modified (sans corps),
     * ce qui economise la bande passante et la serialisation.
     */
    @Bean
    Filter filtreEtag() {
        return new ShallowEtagHeaderFilter();
    }

    /**
     * En-tete de cache des reponses dynamiques (API et pages HTML) :
     * no-cache = la reponse peut etre stockee mais doit etre revalidee a
     * chaque requete via son ETag (d'ou le 304 quand rien n'a change). Les
     * ressources statiques gardent leur propre regle (max-age, voir
     * application.properties).
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void postHandle(HttpServletRequest requete, HttpServletResponse reponse,
                    Object gestionnaire, ModelAndView modele) {
                if ("GET".equalsIgnoreCase(requete.getMethod())
                        && !reponse.containsHeader(HttpHeaders.CACHE_CONTROL)) {
                    reponse.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
                }
            }
        });
    }
}
