// src/services/paymentService.js
import { getCommandeById, updateCommandePaymentStatus } from '../clients/commandeClient.js';
import { getUserById, verifyCommercant } from '../clients/userClient.js';
import Payment from '../models/payment.js';
import { generateInvoice } from './invoiceService.js';

export const createPayment = async (commandeId) => {
  // Fetch command details from commande service
  const commande = await getCommandeById(commandeId);
  
  // Verify merchant exists
  // const isCommercantValid = await verifyCommercant(commande.idCommercant);
  // if (!isCommercantValid) {
  //   throw new Error("Invalid merchant for commande");
  // }

  // // Get delivery person details
  // const livreur = await getUserById(commande.idLivreur);

  const paymentData = {
    commande_id: commandeId,
    total_amount:  commande.montantTotal,
    
    status: "pending",
    splits: [
      {
        recipient_type: "merchant", //  to do : change to real merchant id 
        recipient_id: "2",
        amount: 0,
        // amount : 100,
        status: "pending"
      },
      {
        recipient_type: "livreur", // after we link this ms with livraisn ms wil change this to real id 
        recipient_id: "3",
        amount: commande.montantTotal * 0.2, // 20% to delivery
        // amount : 200,
        status: "pending"
      }
    ],
    checkout_url: `https://mock-checkout.com/${commandeId}`
  };

  const payment = await Payment.create(paymentData);
  return payment;
};

export const handlePaymentSuccess = async (commandeId) => {
  // Update payment status
  const updatedPayment = await Payment.findOneAndUpdate(
    { commande_id: commandeId },
    {
      status: 'paid',
      'splits.0.status': 'paid',
      'splits.1.status': 'paid'
    },
    { new: true }
  );

  if (!updatedPayment) {
    throw new Error('Payment not found');
  }

  // Update commande service
  await updateCommandePaymentStatus(commandeId, 'PAID');

  // Generate invoice
  const invoice = await generateInvoice(commandeId);
  
  return { payment: updatedPayment, invoice };
};