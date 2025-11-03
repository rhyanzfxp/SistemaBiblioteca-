import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import mongoose from 'mongoose';
import authRoutes from './routes/auth.js';
import bookRoutes from './routes/books.js';
import userRoutes from './routes/users.js';
import passwordRoutes from './routes/password.js';

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.get('/health', (req,res)=> res.json({ ok:true }));
app.use('/auth', authRoutes);
app.use('/auth', passwordRoutes);
app.use('/books', bookRoutes);
app.use('/users', userRoutes);

const PORT = process.env.PORT || 8080;
const MONGODB_URI = process.env.MONGODB_URI;

mongoose.connect(MONGODB_URI).then(()=>{
  console.log(' MongoDB conectado');
  app.listen(PORT, ()=> console.log(` API na porta ${PORT}`));
}).catch(err=>{
  console.error('Erro MongoDB', err);
  process.exit(1);
});
