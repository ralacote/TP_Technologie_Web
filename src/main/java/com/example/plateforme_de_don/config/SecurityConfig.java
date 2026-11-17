package com.example.plateforme_de_don.config;

import com.example.plateforme_de_don.service.UtilisateurDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

/**
 * Sécurité de l'API : authentification HTTP Basic sans état (compatible
 * répartition de charge) et règles d'accès par ressource.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Consultation publique, écriture réservée aux utilisateurs authentifiés. */
    @Bean
    SecurityFilterChain chaineFiltres(HttpSecurity http, AuthenticationProvider fournisseur)
            throws Exception {
        // Point d'entrée Basic : la boîte de connexion du navigateur n'est
        // demandée que par les pages protégées ; le realm affiché précise
        // l'identifiant attendu (email ou pseudo)
        BasicAuthenticationEntryPoint pointEntree = new BasicAuthenticationEntryPoint();
        pointEntree.setRealmName("Plateforme de dons - identifiant : email ou pseudo");
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Sans état : aucune session serveur, la répartition de charge reste simple
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Basic sans échec bloquant : si le navigateur renvoie des
                // identifiants invalides (restés en cache), la requête continue
                // en anonyme au lieu de répondre 401 sur les pages publiques
                // (sinon la boîte de connexion s'affiche en boucle dès l'ouverture)
                .addFilterBefore(new BasicAuthenticationFilter(new ProviderManager(fournisseur)),
                        UsernamePasswordAuthenticationFilter.class)
                // les pages protégées répondent 401 avec la boîte de connexion
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(pointEntree))
                .authorizeHttpRequests(auth -> auth
                        // inscription (API et interface web) et consultation sont publiques
                        .requestMatchers(HttpMethod.POST, "/utilisateurs", "/inscription")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/annonce/**",
                                "/inscription").permitAll()
                        .requestMatchers(HttpMethod.GET, "/annonces", "/annonces/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/utilisateurs/**").permitAll()
                        // ressources statiques de l'interface web
                        .requestMatchers("/css/**", "/favicon.ico").permitAll()
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
