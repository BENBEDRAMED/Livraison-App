package com.example.ms_commande.controller;

import com.example.ms_commande.model.ArticleCommande;
import com.example.ms_commande.model.Commande;
import com.example.ms_commande.model.PaymentStatus;
import com.example.ms_commande.repository.CommandeRepository;
import com.example.ms_commande.service.CommandeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    @Autowired
    private CommandeService commandeService;
    @Autowired
    private CommandeRepository commandeRepository;

    @GetMapping
    public ResponseEntity<List<Commande>> getAllCommandes() {
        return ResponseEntity.ok(commandeService.getAllCommandes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Commande> getCommandeById(@PathVariable Integer id) {
        return commandeService.getCommandeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/client/{idClient}")
    public ResponseEntity<List<Commande>> getCommandesByClient(@PathVariable Integer idClient) {
        return ResponseEntity.ok(commandeService.getCommandesByClient(idClient));
    }
    
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Commande>> getCommandesByStatut(@PathVariable String statut) {
        return ResponseEntity.ok(commandeService.getCommandesByStatut(statut));
    }

    @PostMapping("/client/{idClient}")
    public ResponseEntity<Commande> creerCommande(@PathVariable Integer idClient, @RequestBody List<ArticleCommande> articles) {
        Commande commande = commandeService.creerCommande(idClient, articles);
        return ResponseEntity.status(HttpStatus.CREATED).body(commande);
    }
    
    @PostMapping("/{id}/valider")
    public ResponseEntity<Void> validerCommande(@PathVariable Integer id) {
        boolean success = commandeService.validerCommande(id);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    
    @PostMapping("/{id}/annuler")
    public ResponseEntity<Void> annulerCommande(@PathVariable Integer id) {
        boolean success = commandeService.annulerCommande(id);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    
    @GetMapping("/{id}/total")
    public ResponseEntity<Map<String, Float>> calculerTotal(@PathVariable Integer id) {
        Float total = commandeService.calculerTotalCommande(id);
        return ResponseEntity.ok(Map.of("total", total));
    }
    
    @PostMapping("/{id}/articles")
    public ResponseEntity<Void> ajouterArticle(@PathVariable Integer id, @RequestBody ArticleCommande article) {
        commandeService.ajouterArticle(id, article);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // to check payment statut
//
//     CommandeController.java
    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<Commande> updatePaymentStatus(
            @PathVariable Integer id,
            @RequestParam PaymentStatus paymentStatus) {

        Commande commande = commandeService.getCommandeById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        commande.setPaymentStatus(paymentStatus);
        return ResponseEntity.ok(commandeRepository.save(commande));
    }
}

