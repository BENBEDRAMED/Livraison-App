import app from './app.js';
import connectDB from './db.js';
import eurekaClient from './config/eureka-client.js';

const PORT = process.env.PORT || 5000;

connectDB().then(() => {
  app.listen(PORT, () => {
    console.log(`Payment service running on http://localhost:${PORT}`);

    // Start Eureka client
    eurekaClient.start(error => {
      if (error) {
        console.log('Eureka registration failed:', error);
      } else {
        console.log('Successfully registered with Eureka');
      }
    });
  });
});

// Handle graceful shutdown
process.on('SIGINT', () => {
  eurekaClient.stop();
  console.log('Unregistered from Eureka');
  process.exit();
});

