package com.example.ms_user.controller;

import com.example.ms_user.model.*;
import com.example.ms_user.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // ✅ Inscription d'un client (inscription directe)
    @PostMapping("/inscription/client")
    public ResponseEntity<Client> inscrireClient(@RequestBody Client client) {
        return ResponseEntity.ok(utilisateurService.inscrireClient(client));
    }

    // ✅ Soumettre une demande d'inscription (livreur ou commerçant)
    @PostMapping("/inscription/demande")
    public ResponseEntity<DemandeInscription> soumettreDemande(@RequestBody DemandeInscription demande) {
        return ResponseEntity.ok(utilisateurService.soumettreDemande(demande));
    }

    // ✅ Lister toutes les demandes en attente (ADMIN)
    @GetMapping("/demandes/en-attente")
    public ResponseEntity<List<DemandeInscription>> getDemandesEnAttente() {
        return ResponseEntity.ok(utilisateurService.getDemandesEnAttente());
    }

    // ✅ Accepter ou refuser une demande (ADMIN)
    @PostMapping("/demandes/{id}/decision")
    public ResponseEntity<String> gererDemande(@PathVariable int id, @RequestParam boolean accepter) {
        return ResponseEntity.ok(utilisateurService.gererDemande(id, accepter));
    }

    // ✅ Récupérer un utilisateur par ID
    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getUtilisateurById(@PathVariable int id) {
        Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurById(id);
        return utilisateur.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ Récupérer un utilisateur par email
    @GetMapping("/email/{email}")
    public ResponseEntity<Utilisateur> getUtilisateurByEmail(@PathVariable String email) {
        Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurByEmail(email);
        return utilisateur.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ Lister tous les utilisateurs (ADMIN)
    @GetMapping("/")
    public ResponseEntity<List<Utilisateur>> getAllUtilisateurs() {
        return ResponseEntity.ok(utilisateurService.getAllUtilisateurs());
    }

    // ✅ Mettre à jour un utilisateur (Seul l'utilisateur peut modifier ses infos)
    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> mettreAJourUtilisateur(@PathVariable int id, @RequestBody Utilisateur newUser) {
        return ResponseEntity.ok(utilisateurService.mettreAJourUtilisateur(id, newUser));
    }

    // ✅ Supprimer un utilisateur (Seul l'utilisateur ou un admin peut supprimer)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerUtilisateur(@PathVariable int id) {
        utilisateurService.supprimerUtilisateur(id);
        return ResponseEntity.ok("Utilisateur supprimé avec succès !");
    }

    // ✅ Récupérer les infos de l'utilisateur actuellement connecté
    @GetMapping("/me")
    public ResponseEntity<Utilisateur> getCurrentUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return utilisateurService.getUtilisateurByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/commercants")
    public ResponseEntity<List<Utilisateur>> getAllCommercants() {
        return ResponseEntity.ok(utilisateurService.getUtilisateursByRole(Role.ROLE_COMMERCANT));
    }

    // ✅ Vérifier existence d’un commerçant
    @GetMapping("/commercant/{id}/exists")
    public ResponseEntity<Boolean> commercantExists(@PathVariable int id) {
        Optional<Utilisateur> user = utilisateurService.getUtilisateurById(id);
        return ResponseEntity.ok(user.isPresent() && user.get().getRole() == Role.ROLE_COMMERCANT);
    }
}
