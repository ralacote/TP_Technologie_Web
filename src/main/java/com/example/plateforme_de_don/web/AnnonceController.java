package com.example.plateforme_de_don.web;

import com.example.plateforme_de_don.dto.AnnonceDto;
import com.example.plateforme_de_don.dto.AnnonceInput;
import com.example.plateforme_de_don.mapper.AnnonceRepresentation;
import com.example.plateforme_de_don.model.Annonce;
import com.example.plateforme_de_don.model.EtatObjet;
import com.example.plateforme_de_don.model.ModeLivraison;
import com.example.plateforme_de_don.service.AnnonceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ressource REST des annonces : CRUD complet, JSON et HAL via négociation de contenu.
 */
@RestController
@RequestMapping("/annonces")
public class AnnonceController {

    private final AnnonceService service;
    private final AnnonceRepresentation representation;

    public AnnonceController(AnnonceService service, AnnonceRepresentation representation) {
        this.service = service;
        this.representation = representation;
    }

    @GetMapping
    public PagedModel<EntityModel<AnnonceDto>> liste(
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String motCle,
            @RequestParam(required = false) EtatObjet etat,
            @RequestParam(required = false) ModeLivraison mode,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10, sort = "datePublication", direction = Sort.Direction.DESC)
            Pageable pageable,
            PagedResourcesAssembler<Annonce> assembler) {
        Page<Annonce> page = service.recherche(zone, motCle, etat, mode, q, pageable);
        return assembler.toModel(page, representation);
    }

    @GetMapping("/{id}")
    public EntityModel<AnnonceDto> detail(@PathVariable Long id) {
        return representation.toModel(service.parId(id));
    }

    @PostMapping
    public ResponseEntity<EntityModel<AnnonceDto>> cree(@Valid @RequestBody AnnonceInput input) {
        EntityModel<AnnonceDto> modele = representation.toModel(service.cree(input));
        return ResponseEntity.created(modele.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(modele);
    }

    @PutMapping("/{id}")
    public EntityModel<AnnonceDto> modifie(@PathVariable Long id,
            @Valid @RequestBody AnnonceInput input) {
        return representation.toModel(service.modifie(id, input));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprime(@PathVariable Long id) {
        service.supprime(id);
        return ResponseEntity.noContent().build();
    }
}
