package com.example.plateforme_de_don.web;

import com.example.plateforme_de_don.dto.AnnonceDto;
import com.example.plateforme_de_don.dto.AnnonceInput;
import com.example.plateforme_de_don.dto.MessageInput;
import com.example.plateforme_de_don.dto.UtilisateurInput;
import com.example.plateforme_de_don.mapper.AnnonceRepresentation;
import com.example.plateforme_de_don.model.Annonce;
import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import com.example.plateforme_de_don.model.Utilisateur;
import com.example.plateforme_de_don.service.AnnonceService;
import com.example.plateforme_de_don.service.EmailDejaUtiliseException;
import com.example.plateforme_de_don.service.FavoriDejaExistantException;
import com.example.plateforme_de_don.service.FavoriService;
import com.example.plateforme_de_don.service.LotService;
import com.example.plateforme_de_don.service.MessageService;
import com.example.plateforme_de_don.service.PseudoDejaUtiliseException;
import com.example.plateforme_de_don.service.UtilisateurService;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Interface Web simple (HTML, rendu côte serveur) : pages consultables dans un
 * navigateur, réutilisant les mêmes services que l'API REST. Les chemins sont
 * distincts de ceux de l'API pour ne pas interférer avec elle.
 */
@Controller
public class PageController {

    private final AnnonceService annonces;
    private final UtilisateurService utilisateurs;
    private final FavoriService favoris;
    private final LotService lots;
    private final MessageService messages;
    private final AnnonceRepresentation representation;

    public PageController(AnnonceService annonces, UtilisateurService utilisateurs,
            FavoriService favoris, LotService lots, MessageService messages,
            AnnonceRepresentation representation) {
        this.annonces = annonces;
        this.utilisateurs = utilisateurs;
        this.favoris = favoris;
        this.lots = lots;
        this.messages = messages;
        this.representation = representation;
    }

    /** Visiteur connecté ? (affiché dans la navigation des pages) */
    @ModelAttribute("authentifie")
    public boolean authentifie(@AuthenticationPrincipal UserDetails utilisateurConnecte) {
        return utilisateurConnecte != null;
    }

    /** Accueil : liste des annonces avec filtres de recherche (page publique). */
    @GetMapping("/")
    public String accueil(@RequestParam(required = false) String zone,
            @RequestParam(required = false) String motCle,
            @RequestParam(required = false) EtatObjet etat,
            @RequestParam(required = false) ModeLivraison mode,
            Model model) {
        List<AnnonceDto> resultat = annonces
                .recherche(zone, motCle, etat, mode, null, PageRequest.of(0, 20))
                .map(annonce -> representation.toModel(annonce).getContent()).toList();
        model.addAttribute("annonces", resultat);
        model.addAttribute("etats", EtatObjet.values());
        model.addAttribute("modes", ModeLivraison.values());
        return "accueil";
    }

    /** Détail d'une annonce (page publique). */
    @GetMapping("/annonce/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Annonce annonce = annonces.parId(id);
        model.addAttribute("annonce", representation.toModel(annonce).getContent());
        return "annonce";
    }

    /** Mise en favori depuis la page de détail (redirige vers la même page). */
    @PostMapping("/annonce/{id}/favori")
    public String ajouteFavori(@AuthenticationPrincipal UserDetails utilisateurConnecte,
            @PathVariable Long id) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        try {
            favoris.ajoute(utilisateur, id);
        } catch (FavoriDejaExistantException e) {
            // déjà en favori : on retourne simplement à la page de l'annonce
        }
        return "redirect:/annonce/" + id;
    }

    /** Message au donneur envoyé depuis la page de détail. */
    @PostMapping("/annonce/{id}/message")
    public String envoieMessage(@AuthenticationPrincipal UserDetails utilisateurConnecte,
            @PathVariable Long id, @RequestParam String contenu) {
        if (contenu == null || contenu.isBlank()) {
            return "redirect:/annonce/" + id;
        }
        Utilisateur expediteur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        Annonce annonce = annonces.parId(id);
        messages.envoie(expediteur, new MessageInput(id, annonce.getDonneur().getId(), contenu));
        return "redirect:/annonce/" + id;
    }

    /** Formulaire de publication d'une annonce (utilisateur authentifié). */
    @GetMapping("/publier")
    public String formulairePublication(Model model) {
        model.addAttribute("etats", EtatObjet.values());
        model.addAttribute("modes", ModeLivraison.values());
        return "publier";
    }

    /** Création d'une annonce depuis le formulaire web. */
    @PostMapping("/publier")
    public String publie(@AuthenticationPrincipal UserDetails utilisateurConnecte,
            @RequestParam String titre, @RequestParam String description,
            @RequestParam EtatObjet etat, @RequestParam ModeLivraison modeLivraison,
            @RequestParam(required = false) String zoneGeographique,
            @RequestParam(required = false) String motCles) {
        Utilisateur donneur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        Set<String> mots = motCles == null || motCles.isBlank() ? null
                : Arrays.stream(motCles.split(",")).map(String::trim)
                        .filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        AnnonceInput input = new AnnonceInput(titre, description, etat, modeLivraison,
                zoneGeographique, mots);
        Annonce annonce = annonces.cree(donneur, input);
        return "redirect:/annonce/" + annonce.getId();
    }

    /** Formulaire d'inscription (page publique). */
    @GetMapping("/inscription")
    public String formulaireInscription() {
        return "inscription";
    }

    /** Inscription depuis le formulaire web (retour à l'accueil si OK). */
    @PostMapping("/inscription")
    public String inscris(@RequestParam String email, @RequestParam String pseudo,
            @RequestParam String motDePasse) {
        try {
            utilisateurs.cree(new UtilisateurInput(email, pseudo, motDePasse));
        } catch (EmailDejaUtiliseException e) {
            return "redirect:/inscription?erreur=email";
        } catch (PseudoDejaUtiliseException e) {
            return "redirect:/inscription?erreur=pseudo";
        }
        return "redirect:/";
    }

    /** Espace personnel : annonces favorites de l'utilisateur connecté. */
    @GetMapping("/mes-favoris")
    public String mesFavoris(@AuthenticationPrincipal UserDetails utilisateurConnecte,
            Model model) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        model.addAttribute("annonces", favoris.liste(utilisateur).stream()
                .map(favori -> representation.toModel(favori.getAnnonce()).getContent())
                .toList());
        return "favoris";
    }

    /** Espace personnel : lots de l'utilisateur connecté. */
    @GetMapping("/mes-lots")
    public String mesLots(@AuthenticationPrincipal UserDetails utilisateurConnecte,
            Model model) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        model.addAttribute("lots", lots.liste(utilisateur));
        return "lots";
    }

    /** Espace personnel : messages reçus et envoyés. */
    @GetMapping("/mes-messages")
    public String mesMessages(@AuthenticationPrincipal UserDetails utilisateurConnecte,
            Model model) {
        Utilisateur utilisateur = utilisateurs.parEmail(utilisateurConnecte.getUsername());
        model.addAttribute("recus", messages.recus(utilisateur));
        model.addAttribute("envoyes", messages.envoyes(utilisateur));
        return "messages";
    }
}
