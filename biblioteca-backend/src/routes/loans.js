    import express from 'express';
    import { requireAuth } from '../middlewares/auth.js';
    import Loan from '../models/Loan.js';

    const router = express.Router();


    router.post('/', requireAuth, async (req, res) => {
      try {
        const { bookId } = req.body || {};
        if (!bookId) return res.status(400).json({ error: 'bookId é obrigatório' });

        const exists = await Loan.findOne({
          userId: req.user.id,
          bookId,
          status: { $in: ['PENDENTE','APROVADO','RENOVADO'] }
        });
        if (exists) return res.status(400).json({ error: 'Já há solicitação/emprestado deste livro' });

        const loan = await Loan.create({ userId: req.user.id, bookId });
        res.json(loan);
      } catch (e) {
        res.status(500).json({ error: 'Falha ao solicitar' });
      }
    });


   router.get('/me', requireAuth, async (req, res) => {
     const where = { userId: req.user.id };
     if (req.query.active === 'true') where.status = { $in: ['APROVADO','RENOVADO'] };

     const items = await Loan.find(where)
       .sort({ requestedAt: -1 })
       .populate('bookId','title author coverUrl')
       .populate('userId','name email');

     res.json(items);
   });


    export default router;
