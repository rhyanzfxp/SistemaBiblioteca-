import express from 'express';
import bcrypt from 'bcryptjs';
import fs from 'fs-extra';
import path from 'path';
import User from '../models/User.js';
import { requireAuth, requireAdmin } from '../middlewares/auth.js';
import { avatarUpload } from '../lib/upload.js';

const router = express.Router();

router.get('/me', requireAuth, async (req, res) => {
  const u = await User.findById(req.user.id).select('-passwordHash');
  if (!u) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(u);
});

router.patch('/me', requireAuth, async (req, res) => {
  const data = {};
  if (typeof req.body.name === 'string') data.name = req.body.name.trim();
  if (typeof req.body.email === 'string') data.email = req.body.email.trim().toLowerCase();
  if (typeof req.body.photoUrl === 'string') data.photoUrl = req.body.photoUrl.trim();

  const u = await User.findByIdAndUpdate(req.user.id, data, { new: true, runValidators: true })
    .select('-passwordHash');
  if (!u) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(u);
});


router.post('/me/photo', requireAuth, avatarUpload.single('photo'), async (req, res) => {
  try {
    if (!req.file) return res.status(400).json({ error: 'Envie a imagem no campo "photo"' });

    const user = await User.findById(req.user.id);
    if (!user) return res.status(404).json({ error: 'Usuário não encontrado' });


    if (user.photoUrl && user.photoUrl.startsWith('/uploads/')) {
      const oldAbs = path.resolve('.', user.photoUrl.replace(/^\//, ''));
      await fs.remove(oldAbs).catch(() => {});
    }

    const publicPath = `/uploads/avatars/${req.file.filename}`;
    user.photoUrl = publicPath;
    await user.save();

    const safe = user.toObject();
    delete safe.passwordHash;
    res.json(safe);
  } catch (e) {
    console.error('POST /me/photo', e);
    res.status(500).json({ error: 'Erro ao processar upload' });
  }
});


router.delete('/me/photo', requireAuth, async (req, res) => {
  try {
    const user = await User.findById(req.user.id);
    if (!user) return res.status(404).json({ error: 'Usuário não encontrado' });

    if (user.photoUrl && user.photoUrl.startsWith('/uploads/')) {
      const oldAbs = path.resolve('.', user.photoUrl.replace(/^\//, ''));
      await fs.remove(oldAbs).catch(() => {});
    }

    user.photoUrl = '';
    await user.save();

    const safe = user.toObject();
    delete safe.passwordHash;
    res.json(safe);
  } catch (e) {
    console.error('DELETE /me/photo', e);
    res.status(500).json({ error: 'Erro interno' });
  }
});

router.get('/me/accessibility', requireAuth, async (req, res) => {
  const u = await User.findById(req.user.id).select('accessibility');
  if (!u) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(u.accessibility);
});

router.patch('/me/accessibility', requireAuth, async (req, res) => {
  const { fontSize, contrast, voiceAssist } = req.body;
  const u = await User.findByIdAndUpdate(
    req.user.id,
    { accessibility: { fontSize, contrast, voiceAssist } },
    { new: true, runValidators: true }
  ).select('accessibility');
  if (!u) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(u.accessibility);
});

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

router.delete('/:id', requireAuth, requireAdmin, async (_req, res) => {
  await User.findByIdAndDelete(_req.params.id);
  res.json({ ok: true });
});

export default router;
