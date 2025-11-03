import express from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import User from '../models/User.js';
import { requireAuth } from '../middleware/auth.js';

const router = express.Router();


router.post('/register', async (req, res) => {
  try {
    const { name, email, password } = req.body || {};
    if (!name || !email || !password) {
      return res.status(400).json({ error: 'Campos obrigatórios: name, email, password' });
    }

    const exists = await User.findOne({ email: email.trim().toLowerCase() });
    if (exists) return res.status(409).json({ error: 'E-mail já cadastrado' });

    const passwordHash = bcrypt.hashSync(password, 10);
    const user = await User.create({
      name: name.trim(),
      email: email.trim().toLowerCase(),
      passwordHash,
      role: 'user',
      active: true
    });

    return res.status(201).json({ ok: true, id: user._id });
  } catch (e) {
    console.error('REGISTER ERROR', e);
    return res.status(500).json({ error: 'Erro interno' });
  }
});


router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body || {};
    const user = await User.findOne({ email: (email || '').trim().toLowerCase() });

    if (!user || !user.active) return res.status(401).json({ error: 'Credenciais inválidas' });

    const ok = bcrypt.compareSync(password || '', user.passwordHash);
    if (!ok) return res.status(401).json({ error: 'Credenciais inválidas' });

    const token = jwt.sign(
      { id: user._id.toString(), role: user.role, name: user.name },
      process.env.JWT_SECRET,
      { expiresIn: '7d' }
    );

    return res.json({
      token,
      user: { id: user._id, name: user.name, email: user.email, role: user.role, active: user.active }
    });
  } catch (e) {
    console.error('LOGIN ERROR', e);
    return res.status(500).json({ error: 'Erro interno' });
  }
});


router.post('/admin/login', async (req, res) => {
  try {
    const { email, password } = req.body || {};
    const user = await User.findOne({ email: (email || '').trim().toLowerCase(), role: 'admin' });

    if (!user || !user.active) return res.status(401).json({ error: 'Credenciais inválidas' });

    const ok = bcrypt.compareSync(password || '', user.passwordHash);
    if (!ok) return res.status(401).json({ error: 'Credenciais inválidas' });

    const token = jwt.sign(
      { id: user._id.toString(), role: user.role, name: user.name },
      process.env.JWT_SECRET,
      { expiresIn: '7d' }
    );

    return res.json({
      token,
      user: { id: user._id, name: user.name, email: user.email, role: user.role, active: user.active }
    });
  } catch (e) {
    console.error('ADMIN LOGIN ERROR', e);
    return res.status(500).json({ error: 'Erro interno' });
  }
});


router.get('/me', requireAuth, async (req, res) => {
  const me = await User.findById(req.user.id).select('-passwordHash');
  if (!me) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(me);
});

export default router;
