package com.example.ms_commande.controller;

import com.example.ms_commande.model.Categorie;
import com.example.ms_commande.model.Produit;
import com.example.ms_commande.service.CategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategorieController {

    @Autowired
    private CategorieService categorieService;

    @GetMapping
    public ResponseEntity<List<Categorie>> getAllCategories() {
        return ResponseEntity.ok(categorieService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categorie> getCategorieById(@PathVariable Integer id) {
        return categorieService.getCategorieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Categorie> createCategorie(@RequestBody Categorie categorie) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categorieService.saveCategorie(categorie));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categorie> updateCategorie(@PathVariable Integer id, @RequestBody Categorie categorie) {
        return categorieService.getCategorieById(id)
                .map(existingCategorie -> {
                    categorie.setId(id);
                    return ResponseEntity.ok(categorieService.saveCategorie(categorie));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategorie(@PathVariable Integer id) {
        if (categorieService.getCategorieById(id).isPresent()) {
            categorieService.deleteCategorie(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{categorieId}/produits")
    public ResponseEntity<Void> ajouterProduit(@PathVariable Integer categorieId, @RequestBody Produit produit) {
        try {
            categorieService.ajouterProduit(categorieId, produit);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{categorieId}/produits/{produitId}")
    public ResponseEntity<Void> supprimerProduit(@PathVariable Integer categorieId, @PathVariable Integer produitId) {
        try {
            categorieService.supprimerProduit(categorieId, produitId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

