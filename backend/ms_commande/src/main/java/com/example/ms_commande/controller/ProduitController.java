    package com.example.ms_commande.controller;

    import com.example.ms_commande.dto.ProduitDTO;
    import com.example.ms_commande.exception.ResourceNotFoundException;
    import com.example.ms_commande.model.Produit;
    import com.example.ms_commande.service.ProduitService;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/api/produits")
    public class ProduitController {

        @Autowired
        private ProduitService produitService;

        @GetMapping
        public ResponseEntity<List<Produit>> getAllProduits() {
            return ResponseEntity.ok(produitService.getAllProduits());
        }

        @GetMapping("/with-commercant")
        public ResponseEntity<List<ProduitDTO>> getAllProduitsWithCommercantInfo() {
            return ResponseEntity.ok(produitService.getAllProduitsWithCommercantInfo());
        }

        @GetMapping("/{id}")
        public ResponseEntity<Produit> getProduitById(@PathVariable Integer id) {
            return produitService.getProduitById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        @GetMapping("/{id}/details")
        public ResponseEntity<ProduitDTO> getProduitDetailsById(@PathVariable Integer id) {
            try {
                ProduitDTO produitDTO = produitService.getProduitDTOById(id);
                return ResponseEntity.ok(produitDTO);
            } catch (ResourceNotFoundException e) {
                return ResponseEntity.notFound().build();
            }
        }

        @GetMapping("/categorie/{categorieId}")
        public ResponseEntity<List<Produit>> getProduitsByCategorie(@PathVariable Integer categorieId) {
            return ResponseEntity.ok(produitService.getProduitsByCategorie(categorieId));
        }

        @GetMapping("/commercant/{idCommercant}")
        public ResponseEntity<List<ProduitDTO>> getProduitsByCommercant(@PathVariable Integer idCommercant) {
            return ResponseEntity.ok(produitService.getProduitsByCommercant(idCommercant));
        }

        private static final Logger log = LoggerFactory.getLogger(ProduitController.class);

        @PostMapping
        public ResponseEntity<Produit> createProduit(@RequestBody Produit produit) {
            try {
                log.info("Requête de création reçue avec les données suivantes : {}", produit);
                return ResponseEntity.status(HttpStatus.CREATED).body(produitService.saveProduit(produit));
            } catch (ResourceNotFoundException e) {
                log.error("Erreur : ressource non trouvée", e);
                return ResponseEntity.badRequest().build();
            } catch (Exception e) {
                log.error("Une erreur inattendue s'est produite", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        @PutMapping("/{id}")
        public ResponseEntity<Produit> updateProduit(@PathVariable Integer id, @RequestBody Produit produit) {
            try {
                return produitService.getProduitById(id)
                        .map(existingProduit -> {
                            produit.setId(id);
                            return ResponseEntity.ok(produitService.saveProduit(produit));
                        })
                        .orElse(ResponseEntity.notFound().build());
            } catch (ResourceNotFoundException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteProduit(@PathVariable Integer id) {
            if (produitService.getProduitById(id).isPresent()) {
                produitService.deleteProduit(id);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        }

        @PostMapping("/{id}/stock/ajouter")
        public ResponseEntity<Void> ajouterStock(@PathVariable Integer id, @RequestParam Integer quantite) {
            boolean success = produitService.ajouterStock(id, quantite);
            if (success) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        }

        @PostMapping("/{id}/stock/reduire")
        public ResponseEntity<Void> reduireStock(@PathVariable Integer id, @RequestParam Integer quantite) {
            boolean success = produitService.reduireStock(id, quantite);
            if (success) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

