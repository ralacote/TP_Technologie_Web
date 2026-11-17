package com.example.plateforme_de_don.config;

import com.example.plateforme_de_don.service.UtilisateurDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sécurité de l'API : authentification HTTP Basic sans état (compatible
 * répartition de charge) et règles d'accès par ressource.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Consultation publique, écriture réservée aux utilisateurs authentifiés. */
    @Bean
    SecurityFilterChain chaineFiltres(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Sans état : aucune session serveur, la répartition de charge reste simple
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // inscription et consultation sont publiques
                        .requestMatchers(HttpMethod.POST, "/utilisateurs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/annonces", "/annonces/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/utilisateurs/**").permitAll()
                        // toute écriture exige un utilisateur authentifié
                        .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    PasswordEncoder encodeurMotsDePasse() {
        return new BCryptPasswordEncoder();
    }

    /** Vérifie les identifiants (email + mot de passe BCrypt) depuis la base. */
    @Bean
    AuthenticationProvider fournisseurAuthentification(UtilisateurDetailsService detailsService,
            PasswordEncoder encodeur) {
        DaoAuthenticationProvider fournisseur = new DaoAuthenticationProvider(detailsService);
        fournisseur.setPasswordEncoder(encodeur);
        return fournisseur;
    }
}
