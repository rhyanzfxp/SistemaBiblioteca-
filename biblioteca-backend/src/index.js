import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import mongoose from 'mongoose';

import authRoutes from './routes/auth.js';
import bookRoutes from './routes/books.js';
import userRoutes from './routes/users.js';
import passwordRoutes from './routes/password.js';
import loansRoutes from './routes/loans.js';
import adminLoansRoutes from './routes/admin.loans.js';
import favoritesRoutes from './routes/favorites.js';
import notificationsRoutes from './routes/notifications.js';
import adminNoticesRoutes from './routes/admin.notices.js';

const app = express();


app.use(cors({ origin: '*' }));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));


app.get('/health', (req, res) => res.json({ ok: true }));

// Rotas
app.use('/auth', authRoutes);
app.use('/auth', passwordRoutes);
app.use('/books', bookRoutes);
app.use('/users', userRoutes);
app.use('/loans', loansRoutes);
app.use('/admin/loans', adminLoansRoutes);
app.use('/me/favorites', favoritesRoutes);
app.use('/me/notifications', notificationsRoutes);
app.use('/admin/notices', adminNoticesRoutes);

const PORT = process.env.PORT || 8080;
const MONGODB_URI = process.env.MONGODB_URI || '';

async function start() {

  try {
    if (!MONGODB_URI) {
      console.error(' MONGODB_URI não definida nos envs.');
    } else {
      console.log('🔎 Conectando no MongoDB...');
      await mongoose.connect(MONGODB_URI, {
        serverSelectionTimeoutMS: 12000,
      });
      console.log('✅ MongoDB conectado');
    }
  } catch (err) {
    console.error(' Erro MongoDB:', {
      name: err?.name,
      code: err?.code,
      message: err?.message,
      reason: err?.reason?.message,
    });

  }


  app.listen(PORT, '0.0.0.0', () => {
    console.log(` API rodando na porta ${PORT}`);
  });
}

start();
