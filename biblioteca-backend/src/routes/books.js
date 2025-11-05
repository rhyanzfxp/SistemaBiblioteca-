import express from 'express';
import Book from '../models/Book.js';
import { requireAuth, requireAdmin } from '../middlewares/auth.js';

const router = express.Router();

router.get('/', async (req,res)=>{
  const { q } = req.query;
  const filter = q ? { $or:[ {title: {$regex:q, $options:'i'}}, {author: {$regex:q, $options:'i'}} ] } : {};
  const items = await Book.find(filter).sort({ createdAt:-1 });
  res.json(items);
});

router.get('/:id', async (req,res)=>{
  const b = await Book.findById(req.params.id);
  if (!b) return res.status(404).json({ error:'Not found' });
  res.json(b);
});

router.post('/', requireAuth, requireAdmin, async (req,res)=>{
  const b = await Book.create(req.body);
  res.json(b);
});

router.patch('/:id', requireAuth, requireAdmin, async (req,res)=>{
  const b = await Book.findByIdAndUpdate(req.params.id, req.body, { new:true });
  if (!b) return res.status(404).json({ error:'Not found' });
  res.json(b);
});

router.delete('/:id', requireAuth, requireAdmin, async (req,res)=>{
  await Book.findByIdAndDelete(req.params.id);
  res.json({ ok:true });
});

export default router;
