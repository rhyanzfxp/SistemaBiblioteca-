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
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.get('/health', (req,res)=> res.json({ ok:true }));
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
const MONGODB_URI = process.env.MONGODB_URI;

mongoose.connect(MONGODB_URI).then(()=>{
  console.log(' MongoDB conectado');
  app.listen(PORT, ()=> console.log(` API na porta ${PORT}`));
}).catch(err=>{
  console.error('Erro MongoDB', err);
  process.exit(1);
});
