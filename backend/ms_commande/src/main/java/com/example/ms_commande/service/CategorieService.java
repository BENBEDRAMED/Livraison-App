package com.example.ms_commande.service;

import com.example.ms_commande.model.Categorie;
import com.example.ms_commande.model.Produit;
import com.example.ms_commande.repository.CategorieRepository;
import com.example.ms_commande.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategorieService {

    @Autowired
    private CategorieRepository categorieRepository;
    
    @Autowired
    private ProduitRepository produitRepository;

    public List<Categorie> getAllCategories() {
        return categorieRepository.findAll();
    }

    public Optional<Categorie> getCategorieById(Integer id) {
        return categorieRepository.findById(id);
    }

    public Categorie saveCategorie(Categorie categorie) {
        return categorieRepository.save(categorie);
    }

    public void deleteCategorie(Integer id) {
        categorieRepository.deleteById(id);
    }
    
    @Transactional
    public void ajouterProduit(Integer categorieId, Produit produit) {
        Categorie categorie = categorieRepository.findById(categorieId)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
        
        categorie.ajouterProduit(produit);
        categorieRepository.save(categorie);
    }
    
    @Transactional
    public void supprimerProduit(Integer categorieId, Integer produitId) {
        Categorie categorie = categorieRepository.findById(categorieId)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
        
        categorie.supprimerProduit(produitId);
        produitRepository.deleteById(produitId);
    }
}

