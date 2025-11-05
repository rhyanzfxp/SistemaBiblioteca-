import express from 'express';
import { requireAuth, requireAdmin } from '../middlewares/auth.js';
import Notice from '../models/Notice.js';
import User from '../models/User.js';
import Notification from '../models/Notification.js';

const router = express.Router();

router.post('/', requireAuth, requireAdmin, async (req, res) => {
  try {
    const { title, body } = req.body || {};
    if (!title || !body) {
      return res.status(400).json({ error: 'Título e corpo são obrigatórios' });
    }


    const n = await Notice.create({ title, body });


    const users = await User.find({ active: true  }).select('_id');
    let delivered = 0;
    if (users.length) {
      const payload = users.map(u => ({
        userId: u._id,
        title,
        body,
        read: false,
      }));
      const result = await Notification.insertMany(payload);
      delivered = result.length;
    }

    return res.json({ ok: true, noticeId: n._id, delivered });
  } catch (err) {
    console.error('POST /admin/notices error:', err);
    return res.status(500).json({ error: 'Falha ao enviar avisos' });
  }
});

router.get('/', requireAuth, requireAdmin, async (_req, res) => {
  const items = await Notice.find({}).sort({ createdAt: -1 }).limit(50);
  res.json(items);
});

export default router;
