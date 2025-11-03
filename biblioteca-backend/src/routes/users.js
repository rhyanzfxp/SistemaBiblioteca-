import express from 'express';
import bcrypt from 'bcryptjs';
import User from '../models/User.js';
import { requireAuth, requireAdmin } from '../middleware/auth.js';

const router = express.Router();


router.get('/', requireAuth, requireAdmin, async (req, res) => {
  const limit = Math.min(parseInt(req.query.limit || '20'), 100);
  const page = Math.max(parseInt(req.query.page || '1'), 1);
  const skip = (page - 1) * limit;

  const q = (req.query.q || '').trim();
  const filter = q
    ? {
        $or: [
          { name: { $regex: q, $options: 'i' } },
          { email: { $regex: q, $options: 'i' } }
        ]
      }
    : {};

  const [total, items] = await Promise.all([
    User.countDocuments(filter),
    User.find(filter, { passwordHash: 0 })
      .skip(skip)
      .limit(limit)
      .sort({ createdAt: -1 })
  ]);

  res.json({
    items,
    total,
    page,
    pages: Math.max(Math.ceil(total / limit), 1)
  });
});


router.get('/:id', requireAuth, requireAdmin, async (req, res) => {
  const u = await User.findById(req.params.id).select('-passwordHash');
  if (!u) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(u);
});


router.patch('/:id', requireAuth, requireAdmin, async (req, res) => {
  const data = {};
  if (typeof req.body.name === 'string') data.name = req.body.name.trim();
  if (typeof req.body.email === 'string') data.email = req.body.email.trim().toLowerCase();
  if (req.body.role && ['user', 'admin'].includes(req.body.role)) data.role = req.body.role;

  if (req.body.password) {
    data.passwordHash = bcrypt.hashSync(req.body.password, 10);
  }

  const u = await User.findByIdAndUpdate(req.params.id, data, { new: true, runValidators: true }).select('-passwordHash');
  if (!u) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(u);
});


router.patch('/:id/status', requireAuth, requireAdmin, async (req, res) => {
  if (typeof req.body.active !== 'boolean') {
    return res.status(400).json({ error: 'Campo "active" deve ser boolean' });
  }
  const u = await User.findByIdAndUpdate(
    req.params.id,
    { active: req.body.active },
    { new: true }
  ).select('-passwordHash');
  if (!u) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(u);
});


router.delete('/:id', requireAuth, requireAdmin, async (req, res) => {
  await User.findByIdAndDelete(req.params.id);
  res.json({ ok: true });
});

export default router;
