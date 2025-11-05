import express from 'express';
import { requireAuth } from '../middlewares/auth.js';
import Notification from '../models/Notification.js';

const router = express.Router();

router.get('/', requireAuth, async (req, res) => {
  const onlyUnread = req.query.onlyUnread === 'true';
  const where = { userId: req.user.id };
  if (onlyUnread) where.read = false;
  const items = await Notification.find(where).sort({ createdAt:-1 }).limit(200);
  res.json(items);
});

router.patch('/:id/read', requireAuth, async (req, res) => {
  await Notification.updateOne({ _id: req.params.id, userId: req.user.id }, { $set: { read:true } });
  res.json({ ok:true });
});

export default router;
