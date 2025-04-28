package com.example.ms_commande.service;

import com.example.ms_commande.model.ArticleCommande;
import com.example.ms_commande.model.Commande;
import com.example.ms_commande.model.Produit;
import com.example.ms_commande.repository.ArticleCommandeRepository;
import com.example.ms_commande.repository.CommandeRepository;
import com.example.ms_commande.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommandeService {

    @Autowired
    private CommandeRepository commandeRepository;
    
    @Autowired
    private ArticleCommandeRepository articleCommandeRepository;
    
    @Autowired
    private ProduitRepository produitRepository;

    public List<Commande> getAllCommandes() {
        return commandeRepository.findAll();
    }

    public Optional<Commande> getCommandeById(Integer id) {
        return commandeRepository.findById(id);
    }
    
    public List<Commande> getCommandesByClient(Integer idClient) {
        return commandeRepository.findByIdClient(idClient);
    }
    
    public List<Commande> getCommandesByStatut(String statut) {
        return commandeRepository.findByStatut(statut);
    }

    @Transactional
    public Commande creerCommande(Integer idClient, List<ArticleCommande> articles) {
        Commande commande = new Commande();
        commande.setDate(LocalDateTime.now());
        commande.setIdClient(idClient);
        commande.setStatut("EN_ATTENTE");
        
        Commande savedCommande = commandeRepository.save(commande);
        
        for (ArticleCommande article : articles) {
            Optional<Produit> produitOpt = produitRepository.findById(article.getProduit().getId());
            if (produitOpt.isPresent()) {
                article.setProduit(produitOpt.get());
                savedCommande.addArticle(article);
            } else {
                throw new IllegalArgumentException("Produit with ID " + article.getProduit().getId() + " does not exist.");
            }
        }
        
        return commandeRepository.save(savedCommande);
    }
    
    @Transactional
    public boolean validerCommande(Integer commandeId) {
        Optional<Commande> commandeOpt = commandeRepository.findById(commandeId);
        if (commandeOpt.isPresent()) {
            Commande commande = commandeOpt.get();
            boolean result = commande.validerCommande();
            if (result) {
                commandeRepository.save(commande);
            }
            return result;
        }
        return false;
    }
    
    @Transactional
    public boolean annulerCommande(Integer commandeId) {
        Optional<Commande> commandeOpt = commandeRepository.findById(commandeId);
        if (commandeOpt.isPresent()) {
            Commande commande = commandeOpt.get();
            boolean result = commande.annulerCommande();
            if (result) {
                commandeRepository.save(commande);
            }
            return result;
        }
        return false;
    }
    
    public Float calculerTotalCommande(Integer commandeId) {
        Optional<Commande> commandeOpt = commandeRepository.findById(commandeId);
        return commandeOpt.map(Commande::calculerTotal).orElse(0f);
    }
    
    @Transactional
    public void ajouterArticle(Integer commandeId, ArticleCommande article) {
        Optional<Commande> commandeOpt = commandeRepository.findById(commandeId);
        if (commandeOpt.isPresent()) {
            Commande commande = commandeOpt.get();
            Optional<Produit> produitOpt = produitRepository.findById(article.getProduit().getId());
            if (produitOpt.isPresent()) {
                article.setProduit(produitOpt.get());
                commande.addArticle(article);
                commandeRepository.save(commande);
            }
        }
    }


}

