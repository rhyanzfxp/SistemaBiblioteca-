import express from 'express';
import mongoose from 'mongoose';
import { requireAuth } from '../middlewares/auth.js';
import User from '../models/User.js';
import Book from '../models/Book.js';

const router = express.Router();

router.get('/', requireAuth, async (req, res) => {
  try {
    const user = await User.findById(req.user.id)
      .populate({ path: 'favorites', select: 'title author coverUrl description' })
      .lean();
    res.json(user?.favorites || []);
  } catch (e) {
    console.error('FAVORITES GET', e);
    res.status(500).json({ error: 'Falha ao listar favoritos' });
  }
});

router.post('/:bookId', requireAuth, async (req, res) => {
  try {
    const { bookId } = req.params;
    if (!mongoose.isValidObjectId(bookId)) {
      return res.status(400).json({ error: 'bookId inválido' });
    }
    const exists = await Book.exists({ _id: bookId });
    if (!exists) return res.status(404).json({ error: 'Livro não encontrado' });

    await User.updateOne(
      { _id: req.user.id },
      { $addToSet: { favorites: bookId } }
    );
    res.json({ ok: true });
  } catch (e) {
    console.error('FAVORITES POST', e);
    res.status(500).json({ error: 'Falha ao adicionar favorito' });
  }
});

router.delete('/:bookId', requireAuth, async (req, res) => {
  try {
    const { bookId } = req.params;
    if (!mongoose.isValidObjectId(bookId)) {
      return res.status(400).json({ error: 'bookId inválido' });
    }
    await User.updateOne(
      { _id: req.user.id },
      { $pull: { favorites: bookId } }
    );
    res.json({ ok: true });
  } catch (e) {
    console.error('FAVORITES DELETE', e);
    res.status(500).json({ error: 'Falha ao remover favorito' });
  }
});

export default router;
