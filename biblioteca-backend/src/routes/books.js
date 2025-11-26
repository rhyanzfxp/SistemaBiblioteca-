import express from 'express';
import Book from '../models/Book.js';
import { requireAuth, requireAdmin } from '../middlewares/auth.js';
import { coverUpload } from '../lib/upload.js';
import User from '../models/User.js';
import Loan from '../models/Loan.js';

const router = express.Router();


router.get('/', async (req, res) => {
  try {
    const { q } = req.query;
    const filter = q
      ? { $or: [{ title: { $regex: q, $options: 'i' } }, { author: { $regex: q, $options: 'i' } }] }
      : {};
    const items = await Book.find(filter).sort({ createdAt: -1 });
    res.json(items);
  } catch (e) {
    console.error('BOOKS / ERR', e);
    res.status(500).json({ error: 'Falha ao listar livros' });
  }
});




router.get('/recent', async (req, res) => {
  try {
    const limit = Number(req.query.limit) || 12;
    const items = await Book.find({})
      .sort({ createdAt: -1 })
      .limit(limit)
      .lean();
    res.json(items);
  } catch (e) {
    console.error('RECENT ERR', e);
    res.status(500).json({ error: 'Falha ao listar mais recentes' });
  }
});


router.get('/top-recommended', async (req, res) => {
  try {
    const limit = Number(req.query.limit) || 12;

    const ranked = await User.aggregate([
      { $project: { favorites: 1 } },
      { $unwind: '$favorites' },
      { $group: { _id: '$favorites', favs: { $sum: 1 } } },
      { $sort: { favs: -1 } },
      { $limit: limit },
      {
        $lookup: {
          from: 'books',
          localField: '_id',
          foreignField: '_id',
          as: 'book'
        }
      },
      { $unwind: '$book' },
      { $replaceWith: { $mergeObjects: ['$$ROOT', '$book'] } },
      {
        $project: {
          _id: 1,
          title: 1,
          author: 1,
          coverUrl: 1,
          sector: 1,
          shelfCode: 1,
          description: 1,
          createdAt: 1,
          updatedAt: 1,
          favs: 1
        }
      }
    ]);

    if (!ranked.length) {
      const recent = await Book.find({}).sort({ createdAt: -1 }).limit(limit).lean();
      return res.json(recent);
    }

    res.json(ranked);
  } catch (e) {
    console.error('TOP-RECOMMENDED ERR', e);
    res.status(500).json({ error: 'Falha ao listar recomendados' });
  }
});


router.get('/top-borrowed', async (req, res) => {
  try {
    const limit = Number(req.query.limit) || 12;

    const ranked = await Loan.aggregate([
      { $match: { status: { $ne: 'NEGADO' } } },
      { $group: { _id: '$bookId', total: { $sum: 1 } } },
      { $sort: { total: -1 } },
      { $limit: limit },
      {
        $lookup: {
          from: 'books',
          localField: '_id',
          foreignField: '_id',
          as: 'book'
        }
      },
      { $unwind: '$book' },
      { $replaceWith: { $mergeObjects: ['$$ROOT', '$book'] } },
      {
        $project: {
          _id: 1,
          title: 1,
          author: 1,
          coverUrl: 1,
          sector: 1,
          shelfCode: 1,
          description: 1,
          createdAt: 1,
          updatedAt: 1,
          borrowCount: '$total'
        }
      }
    ]);

    res.json(ranked);
  } catch (e) {
    console.error('TOP-BORROWED ERR', e);
    res.status(500).json({ error: 'Falha ao listar mais emprestados' });
  }
});


router.get('/:id', async (req, res) => {
  try {
    const b = await Book.findById(req.params.id);
    if (!b) return res.status(404).json({ error: 'Not found' });
    res.json(b);
  } catch (e) {
    res.status(400).json({ error: 'ID inválido' });
  }
});

router.post('/', requireAuth, requireAdmin, async (req, res) => {
  const { title, author, isbn, copiesTotal, copiesAvailable, tags, coverUrl, sector, shelfCode, description } = req.body;
  const b = await Book.create({ title, author, isbn, copiesTotal, copiesAvailable, tags, coverUrl, sector, shelfCode, description });
  res.json(b);
});

router.post('/cover', requireAuth, requireAdmin, coverUpload.single('cover'), async (req, res) => {
  if (!req.file) return res.status(400).json({ error: 'Nenhuma imagem enviada' });
  res.json({ coverUrl: `/covers/${req.file.filename}` });
});

router.patch('/:id', requireAuth, requireAdmin, async (req, res) => {
  try {
    const { title, author, isbn, copiesTotal, copiesAvailable, tags, coverUrl, sector, shelfCode, description } = req.body;
    const updateFields = { title, author, isbn, copiesTotal, copiesAvailable, tags, coverUrl, sector, shelfCode, description };


    Object.keys(updateFields).forEach(key => updateFields[key] === undefined && delete updateFields[key]);

    const b = await Book.findByIdAndUpdate(req.params.id, updateFields, { new: true });
    if (!b) return res.status(404).json({ error: 'Not found' });
    res.json(b);
  } catch (e) {
    res.status(400).json({ error: 'ID inválido' });
  }
});

router.delete('/:id', requireAuth, requireAdmin, async (req, res) => {
  try {
    await Book.findByIdAndDelete(req.params.id);
    res.json({ ok: true });
  } catch (e) {
    res.status(400).json({ error: 'ID inválido' });
  }
});

export default router;