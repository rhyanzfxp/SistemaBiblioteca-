import express from 'express';
import { requireAuth, requireAdmin } from '../middlewares/auth.js';
import Loan from '../models/Loan.js';
import Notification from '../models/Notification.js';

const router = express.Router();


const MAX_RENEWS = 1;
const DEFAULT_RENEW_DAYS = 7;


router.post('/', requireAuth, async (req, res) => {
  try {
    const { bookId } = req.body || {};
    if (!bookId) return res.status(400).json({ error: 'bookId é obrigatório' });

    const exists = await Loan.findOne({
      userId: req.user.id,
      bookId,
      status: { $in: ['PENDENTE', 'APROVADO', 'RENOVADO'] }
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
  if (req.query.active === 'true') where.status = { $in: ['APROVADO', 'RENOVADO'] };

  const items = await Loan.find(where)
    .sort({ requestedAt: -1 })
    .populate('bookId', 'title author coverUrl')
    .populate('userId', 'name email');

  res.json(items);
});


router.post('/:id/renew-request', requireAuth, async (req, res) => {
  try {
    const { id } = req.params;
    const addDaysRaw = Number(req.body?.addDays ?? DEFAULT_RENEW_DAYS);
    const userReason = String(req.body?.reason ?? '');

    const loan = await Loan.findById(id);
    if (!loan) return res.status(404).json({ error: 'Empréstimo não encontrado' });

    if (String(loan.userId) !== String(req.user.id)) {
      return res.status(403).json({ error: 'Sem permissão para este empréstimo' });
    }

    if (!['APROVADO', 'RENOVADO'].includes(loan.status)) {
      return res.status(400).json({ error: 'Somente empréstimos ativos podem solicitar renovação' });
    }

    if (loan.returnedAt) {
      return res.status(400).json({ error: 'Empréstimo já devolvido' });
    }

    if (loan.renewalRequested) {
      return res.status(400).json({ error: 'Já existe uma solicitação de renovação pendente' });
    }

    if (loan.renewCount >= MAX_RENEWS) {
      return res.status(400).json({ error: 'Limite de renovações atingido' });
    }

    const addDays = isNaN(addDaysRaw) || addDaysRaw <= 0 ? DEFAULT_RENEW_DAYS : addDaysRaw;

    loan.renewalRequested = true;
    loan.renewalAddDays = addDays;
    loan.renewalReason = userReason;
    loan.renewalRequestedAt = new Date();
    await loan.save();


    await Notification.create({
      userId: loan.userId,
      title: 'Renovação solicitada',
      body: `Seu pedido de renovação foi enviado para revisão do administrador.`
    });

    res.json({
      message: 'Solicitação de renovação enviada',
      loan
    });
  } catch (e) {
    res.status(500).json({ error: 'Falha ao solicitar renovação' });
  }
});

export default router;
