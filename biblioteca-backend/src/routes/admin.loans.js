import express from 'express';
import { requireAuth, requireAdmin } from '../middlewares/auth.js';
import Loan from '../models/Loan.js';
import Book from '../models/Book.js';
import Notification from '../models/Notification.js';

const router = express.Router();


router.get('/', requireAuth, requireAdmin, async (req, res) => {
  const items = await Loan.find({ status:'PENDENTE' })
    .sort({ requestedAt:-1 })
    .populate('bookId','title author')
    .populate('userId','name email');
  res.json(items);
});


router.patch('/:id/approve', requireAuth, requireAdmin, async (req, res) => {
  const days = Number(req.body.days || 7);
  const now = new Date();
  const due = new Date(now.getTime() + days*24*60*60*1000);

  const loan = await Loan.findByIdAndUpdate(
    req.params.id,
    { $set: { status:'APROVADO', approvedAt:now, startDate:now, dueDate:due } },
    { new:true }
  );
  if (!loan) return res.status(404).json({ error:'Empréstimo não encontrado' });

  await Book.updateOne({ _id: loan.bookId, copiesAvailable: { $gt: 0 } }, { $inc: { copiesAvailable:-1 } });
  await Notification.create({ userId: loan.userId, title:'Empréstimo aprovado', body:'Retire o livro na biblioteca.' });

  res.json(loan);
});


router.patch('/:id/deny', requireAuth, requireAdmin, async (req, res) => {
  const reason = req.body.reason || 'Indisponível';
  const loan = await Loan.findByIdAndUpdate(req.params.id, { $set: { status:'NEGADO', reason } }, { new:true });
  if (!loan) return res.status(404).json({ error:'Empréstimo não encontrado' });
  await Notification.create({ userId: loan.userId, title:'Empréstimo negado', body: reason });
  res.json(loan);
});


router.patch('/:id/return', requireAuth, requireAdmin, async (req, res) => {
  const now = new Date();
  const loan = await Loan.findByIdAndUpdate(req.params.id, { $set: { status:'DEVOLVIDO', returnedAt: now } }, { new:true });
  if (!loan) return res.status(404).json({ error:'Empréstimo não encontrado' });
  await Book.updateOne({ _id: loan.bookId }, { $inc: { copiesAvailable: 1 } });
  await Notification.create({ userId: loan.userId, title:'Devolução registrada', body:'Obrigado por devolver no prazo.' });
  res.json(loan);
});


router.patch('/:id/renew', requireAuth, requireAdmin, async (req, res) => {
  const addDays = Number(req.body.days || 7);
  const loan = await Loan.findById(req.params.id);
  if (!loan || !['APROVADO','RENOVADO'].includes(loan.status))
    return res.status(400).json({ error:'Não é possível renovar' });
  if (loan.renewCount >= 1) return res.status(400).json({ error:'Renovação máxima atingida' });

  const base = loan.dueDate || new Date();
  const newDue = new Date(base.getTime() + addDays*24*60*60*1000);
  loan.status = 'RENOVADO';
  loan.dueDate = newDue;
  loan.renewCount += 1;
  await loan.save();
  await Notification.create({ userId: loan.userId, title:'Empréstimo renovado', body:`Novo vencimento: ${newDue.toLocaleDateString()}` });
  res.json(loan);
});

export default router;
