import express from 'express';
import paymentRoutes from './routes/paymentRoutes.js';
import staticDb from './staticDb.js';

const app = express();


app.use(express.json()); // For parsing application/json
app.use(express.urlencoded({ extended: true })); // For parsing form data
app.use((req, res, next) => {
  
    console.log('Commands:', staticDb.commands.map(c => c.id));
    // console.log('Payments:', staticDb.payments.map(p => p.commande_id));
    next();
  });
app.use('/payment', paymentRoutes); // Prefix all payment routes

export default app;