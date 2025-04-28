package com.example.ms_user.service;

import com.example.ms_user.model.*;
import com.example.ms_user.repository.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {

    private final ClientRepository clientRepository;
    private final LivreurRepository livreurRepository;
    private final CommercantRepository commercantRepository;
    private final DemandeInscriptionRepository demandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UtilisateurService(ClientRepository clientRepository,
                              LivreurRepository livreurRepository,
                              CommercantRepository commercantRepository,
                              DemandeInscriptionRepository demandeRepository,
                              UtilisateurRepository utilisateurRepository) {
        this.clientRepository = clientRepository;
        this.livreurRepository = livreurRepository;
        this.commercantRepository = commercantRepository;
        this.demandeRepository = demandeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // ✅ Inscription d'un client (ajout automatique dans la table `client`)
    public Client inscrireClient(Client client) {
        client.setMotDePasse(passwordEncoder.encode(client.getMotDePasse()));  // Hasher le mot de passe
        client.setRole(Role.ROLE_CLIENT);  // Assigner le rôle client
        return clientRepository.save(client);  // Enregistrer en tant que Client (hérite de Utilisateur)
    }

    // ✅ Soumission d'une demande d'inscription (livreur ou commerçant)
    public DemandeInscription soumettreDemande(DemandeInscription demande) {
        demande.setMotDePasse(passwordEncoder.encode(demande.getMotDePasse())); // Hasher le mot de passe
        demande.setStatut(StatutDemande.EN_ATTENTE); // Par défaut, la demande est en attente

        if ("LIVREUR".equalsIgnoreCase(String.valueOf(demande.getRole()))) {
            demande.setRole(Role.ROLE_LIVREUR);
        } else if ("COMMERCANT".equalsIgnoreCase(String.valueOf(demande.getRole()))) {
            demande.setRole(Role.ROLE_COMMERCANT);
        }

        return demandeRepository.save(demande);
    }

    // ✅ Lister toutes les demandes en attente
    public List<DemandeInscription> getDemandesEnAttente() {
        return demandeRepository.findByStatut(StatutDemande.EN_ATTENTE);
    }

    // ✅ Accepter ou refuser une demande d'inscription (ADMIN)
    public String gererDemande(int demandeId, boolean accepter) {
        Optional<DemandeInscription> demandeOpt = demandeRepository.findById(demandeId);
        if (demandeOpt.isPresent()) {
            DemandeInscription demande = demandeOpt.get();
            if (accepter) {
                if (demande.getRole() == Role.ROLE_LIVREUR) {
                    Livreur livreur = new Livreur();
                    livreur.setNom(demande.getNom());
                    livreur.setEmail(demande.getEmail());
                    livreur.setNumeroDeTelephone(demande.getNumeroDeTelephone());
                    livreur.setMotDePasse(demande.getMotDePasse());
                    livreur.setRole(Role.ROLE_LIVREUR);
                    livreur.setDisponibilite(true);
                    livreurRepository.save(livreur);
                } else if (demande.getRole() == Role.ROLE_COMMERCANT) {
                    Commercant commercant = new Commercant();
                    commercant.setNom(demande.getNom());
                    commercant.setEmail(demande.getEmail());
                    commercant.setNumeroDeTelephone(demande.getNumeroDeTelephone());
                    commercant.setMotDePasse(demande.getMotDePasse());
                    commercant.setRole(Role.ROLE_COMMERCANT);
                    commercant.setNomBoutique(demande.getNomBoutique());
                    commercant.setAdresse(demande.getAdresse());
                    commercant.setNumRC(demande.getNumRC());
                    commercantRepository.save(commercant);
                }
                demande.setStatut(StatutDemande.ACCEPTEE);
            } else {
                demande.setStatut(StatutDemande.REFUSEE);
            }
            demandeRepository.save(demande);
            return "Demande " + (accepter ? "acceptée" : "refusée");
        }
        return "Demande non trouvée";
    }

    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    // ✅ Récupérer un utilisateur par ID
    public Optional<Utilisateur> getUtilisateurById(int id) {
        return utilisateurRepository.findById(id);
    }

    // ✅ Récupérer un utilisateur par email
    public Optional<Utilisateur> getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    // ✅ Mettre à jour un utilisateur
    public Utilisateur mettreAJourUtilisateur(int id, Utilisateur newUser) {
        return utilisateurRepository.findById(id).map(utilisateur -> {
            utilisateur.setNom(newUser.getNom());
            utilisateur.setEmail(newUser.getEmail());
            utilisateur.setNumeroDeTelephone(newUser.getNumeroDeTelephone());
            if (newUser.getMotDePasse() != null && !newUser.getMotDePasse().isEmpty()) {
                utilisateur.setMotDePasse(passwordEncoder.encode(newUser.getMotDePasse()));  // Mise à jour avec un mot de passe hashé
            }
            return utilisateurRepository.save(utilisateur);
        }).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    // ✅ Supprimer un utilisateur
    public void supprimerUtilisateur(int id) {
        utilisateurRepository.deleteById(id);
    }
    public List<Utilisateur> getUtilisateursByRole(Role role) {
        return utilisateurRepository.findByRole(role);
    }
}
