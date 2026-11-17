package com.example.plateforme_de_don.repository;

import com.example.plateforme_de_don.model.Annonce;
import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/**
 * Critères de filtrage des annonces, combinables entre eux.
 */
public final class AnnonceSpecifications {

    private AnnonceSpecifications() {
    }

    /** Zone géographique contenant le texte donné (insensible à la casse). */
    public static Specification<Annonce> dansZone(String zone) {
        String motif = "%" + zone.trim().toLowerCase(Locale.ROOT) + "%";
        return (annonce, query, cb) -> cb.like(
                cb.lower(annonce.<String>get("zoneGeographique")), motif);
    }

    /** Annonces portant le mot-clé donné. */
    public static Specification<Annonce> avecMotCle(String motCle) {
        String libelle = motCle.trim().toLowerCase(Locale.ROOT);
        return (annonce, query, cb) -> {
            query.distinct(true);
            var join = annonce.join("motCles");
            return cb.equal(cb.lower(join.get("libelle")), libelle);
        };
    }

    /** Annonces dont l'objet est dans l'état donné. */
    public static Specification<Annonce> avecEtat(EtatObjet etat) {
        return (annonce, query, cb) -> cb.equal(annonce.get("etat"), etat);
    }

    /** Annonces livrables selon le mode donné : « main propre » ou « envoi »
     *  incluent aussi les annonces acceptant les deux. */
    public static Specification<Annonce> livrablePar(ModeLivraison mode) {
        return (annonce, query, cb) -> {
            if (mode == ModeLivraison.LES_DEUX) {
                return cb.equal(annonce.get("modeLivraison"), ModeLivraison.LES_DEUX);
            }
            return annonce.get("modeLivraison").in(mode, ModeLivraison.LES_DEUX);
        };
    }

    /** Recherche textuelle dans le titre et la description. */
    public static Specification<Annonce> contientTexte(String texte) {
        String motif = "%" + texte.trim().toLowerCase(Locale.ROOT) + "%";
        return (annonce, query, cb) -> cb.or(
                cb.like(cb.lower(annonce.get("titre")), motif),
                cb.like(cb.lower(annonce.get("description")), motif));
    }
}
