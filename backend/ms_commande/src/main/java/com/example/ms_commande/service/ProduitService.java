package com.example.ms_commande.service;

import com.example.ms_commande.client.UserServiceClient;
import com.example.ms_commande.dto.ProduitDTO;
import com.example.ms_commande.dto.UserDTO;
import com.example.ms_commande.exception.ResourceNotFoundException;
import com.example.ms_commande.model.Produit;
import com.example.ms_commande.repository.ProduitRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des produits.
 * Ce service intègre des appels au microservice utilisateur pour récupérer les informations des commerçants.
 */
@Service
public class ProduitService {

    /**
     * Repository pour accéder aux données des produits.
     */
    @Autowired
    private ProduitRepository produitRepository;
    
    /**
     * Client Feign pour communiquer avec le microservice utilisateur.
     */
    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * Récupère tous les produits sans les informations des commerçants.
     * 
     * @return Liste de tous les produits
     */
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }
    
    /**
     * Récupère tous les produits avec les informations des commerçants.
     * Pour chaque produit, une requête est faite au microservice utilisateur pour récupérer les informations du commerçant.
     * 
     * @return Liste de tous les produits enrichis avec les informations des commerçants
     */
    public List<ProduitDTO> getAllProduitsWithCommercantInfo() {
        List<Produit> produits = produitRepository.findAll();
        List<ProduitDTO> produitDTOs = new ArrayList<>();
        
        for (Produit produit : produits) {
            try {
                // Appel au microservice utilisateur pour récupérer les informations du commerçant
                UserDTO commercant = userServiceClient.getUserById(produit.getIdCommercant());
                produitDTOs.add(ProduitDTO.fromProduit(produit, commercant));
            } catch (FeignException e) {
                // Si le commerçant n'est pas trouvé, on ajoute quand même le produit sans info commerçant
                produitDTOs.add(ProduitDTO.fromProduit(produit, null));
            }
        }
        
        return produitDTOs;
    }

    /**
     * Récupère un produit par son ID.
     * 
     * @param id L'ID du produit à récupérer
     * @return Le produit correspondant à l'ID, encapsulé dans un Optional
     */
    public Optional<Produit> getProduitById(Integer id) {
        return produitRepository.findById(id);
    }
    
    /**
     * Récupère un produit par son ID avec les informations du commerçant.
     * 
     * @param id L'ID du produit à récupérer
     * @return Le produit enrichi avec les informations du commerçant
     * @throws ResourceNotFoundException si le produit n'est pas trouvé
     */
    public ProduitDTO getProduitDTOById(Integer id) {
        // Récupère le produit ou lance une exception si non trouvé
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id: " + id));
        
        try {
            // Appel au microservice utilisateur pour récupérer les informations du commerçant
            UserDTO commercant = userServiceClient.getUserById(produit.getIdCommercant());
            return ProduitDTO.fromProduit(produit, commercant);
        } catch (FeignException e) {
            // Si le commerçant n'est pas trouvé, on retourne le produit sans info commerçant
            return ProduitDTO.fromProduit(produit, null);
        }
    }

    /**
     * Récupère tous les produits d'une catégorie spécifique.
     * 
     * @param categorieId L'ID de la catégorie
     * @return Liste des produits de la catégorie
     */
    public List<Produit> getProduitsByCategorie(Integer categorieId) {
        return produitRepository.findByCategorieId(categorieId);
    }
    
    /**
     * Récupère tous les produits d'un commerçant spécifique avec les informations du commerçant.
     * 
     * @param idCommercant L'ID du commerçant
     * @return Liste des produits du commerçant enrichis avec ses informations
     */
    public List<ProduitDTO> getProduitsByCommercant(Integer idCommercant) {
        List<Produit> produits = produitRepository.findByIdCommercant(idCommercant);
        
        try {
            // Appel au microservice utilisateur pour récupérer les informations du commerçant
            UserDTO commercant = userServiceClient.getUserById(idCommercant);
            // Convertit tous les produits en ProduitDTO avec les informations du commerçant
            return produits.stream()
                    .map(produit -> ProduitDTO.fromProduit(produit, commercant))
                    .collect(Collectors.toList());
        } catch (FeignException e) {
            // Si le commerçant n'est pas trouvé, on retourne les produits sans info commerçant
            return produits.stream()
                    .map(produit -> ProduitDTO.fromProduit(produit, null))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Sauvegarde un produit après avoir vérifié que le commerçant existe.
     * 
     * @param produit Le produit à sauvegarder
     * @return Le produit sauvegardé
     * @throws ResourceNotFoundException si le commerçant n'existe pas ou si le service utilisateur n'est pas disponible
     */
    public Produit saveProduit(Produit produit) {
        // Vérifier si le commerçant existe avant de sauvegarder le produit
        try {
            boolean commercantExists = userServiceClient.commercantExists(produit.getIdCommercant());
            if (!commercantExists) {
                throw new ResourceNotFoundException("Commerçant non trouvé avec l'id: " + produit.getIdCommercant());
            }
            return produitRepository.save(produit);
        } catch (FeignException e) {
            throw new ResourceNotFoundException("Service utilisateur non disponible ou commerçant non trouvé");
        }
    }

    /**
     * Supprime un produit par son ID.
     * 
     * @param id L'ID du produit à supprimer
     */
    public void deleteProduit(Integer id) {
        produitRepository.deleteById(id);
    }
    
    /**
     * Ajoute une quantité spécifiée au stock d'un produit.
     * 
     * @param produitId L'ID du produit
     * @param quantite La quantité à ajouter
     * @return true si l'opération a réussi, false si le produit n'existe pas
     */
    public boolean ajouterStock(Integer produitId, Integer quantite) {
        Optional<Produit> produitOpt = produitRepository.findById(produitId);
        if (produitOpt.isPresent()) {
            Produit produit = produitOpt.get();
            produit.ajouterStock(quantite);
            produitRepository.save(produit);
            return true;
        }
        return false;
    }
    
    /**
     * Réduit le stock d'un produit si la quantité disponible est suffisante.
     * 
     * @param produitId L'ID du produit
     * @param quantite La quantité à retirer
     * @return true si l'opération a réussi, false si le produit n'existe pas ou si le stock est insuffisant
     */
    public boolean reduireStock(Integer produitId, Integer quantite) {
        Optional<Produit> produitOpt = produitRepository.findById(produitId);
        if (produitOpt.isPresent()) {
            Produit produit = produitOpt.get();
            boolean result = produit.reduireStock(quantite);
            if (result) {
                produitRepository.save(produit);
            }
            return result;
        }
        return false;
    }
}

