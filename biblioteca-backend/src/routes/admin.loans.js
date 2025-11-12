import express from 'express';
import { requireAuth, requireAdmin } from '../middlewares/auth.js';
import Loan from '../models/Loan.js';
import Book from '../models/Book.js';
import Notification from '../models/Notification.js';

const router = express.Router();

// Políticas de negócio
const MAX_RENEWS = 1;           // máximo de renovações por empréstimo
const DEFAULT_RENEW_DAYS = 7;   // dias padrão de renovação

// -----------------------------------------------------------------------------
// Lista de empréstimos pendentes de aprovação inicial
// -----------------------------------------------------------------------------
router.get('/', requireAuth, requireAdmin, async (req, res) => {
  const items = await Loan.find({ status: 'PENDENTE' })
    .sort({ requestedAt: -1 })
    .populate('bookId', 'title author')
    .populate('userId', 'name email');
  res.json(items);
});

// -----------------------------------------------------------------------------
// Aprovar empréstimo inicial
// -----------------------------------------------------------------------------
router.patch('/:id/approve', requireAuth, requireAdmin, async (req, res) => {
  const days = Number(req.body.days || 7);
  const now = new Date();
  const due = new Date(now.getTime() + days * 24 * 60 * 60 * 1000);

  const loan = await Loan.findByIdAndUpdate(
    req.params.id,
    { $set: { status: 'APROVADO', approvedAt: now, startDate: now, dueDate: due } },
    { new: true }
  );
  if (!loan) return res.status(404).json({ error: 'Empréstimo não encontrado' });

  await Book.updateOne(
    { _id: loan.bookId, copiesAvailable: { $gt: 0 } },
    { $inc: { copiesAvailable: -1 } }
  );

  await Notification.create({
    userId: loan.userId,
    title: 'Empréstimo aprovado',
    body: 'Retire o livro na biblioteca.'
  });

  res.json(loan);
});

// -----------------------------------------------------------------------------
// Negar empréstimo inicial
// -----------------------------------------------------------------------------
router.patch('/:id/deny', requireAuth, requireAdmin, async (req, res) => {
  const reason = req.body.reason || 'Indisponível';
  const loan = await Loan.findByIdAndUpdate(
    req.params.id,
    { $set: { status: 'NEGADO', reason } },
    { new: true }
  );
  if (!loan) return res.status(404).json({ error: 'Empréstimo não encontrado' });

  await Notification.create({
    userId: loan.userId,
    title: 'Empréstimo negado',
    body: reason
  });

  res.json(loan);
});

// -----------------------------------------------------------------------------
// Registrar devolução
// -----------------------------------------------------------------------------
router.patch('/:id/return', requireAuth, requireAdmin, async (req, res) => {
  const now = new Date();
  const loan = await Loan.findByIdAndUpdate(
    req.params.id,
    { $set: { status: 'DEVOLVIDO', returnedAt: now } },
    { new: true }
  );
  if (!loan) return res.status(404).json({ error: 'Empréstimo não encontrado' });

  await Book.updateOne({ _id: loan.bookId }, { $inc: { copiesAvailable: 1 } });

  await Notification.create({
    userId: loan.userId,
    title: 'Devolução registrada',
    body: 'Obrigado por devolver no prazo.'
  });

  res.json(loan);
});

// -----------------------------------------------------------------------------
// (Opcional) Renovação direta pelo admin (sem solicitação do usuário)
// -----------------------------------------------------------------------------
router.patch('/:id/renew', requireAuth, requireAdmin, async (req, res) => {
  const addDays = Number(req.body.days || DEFAULT_RENEW_DAYS);
  const loan = await Loan.findById(req.params.id);

  if (!loan) return res.status(404).json({ error: 'Empréstimo não encontrado' });
  if (!['APROVADO', 'RENOVADO'].includes(loan.status))
    return res.status(400).json({ error: 'Não é possível renovar' });
  if (loan.returnedAt)
    return res.status(400).json({ error: 'Empréstimo já devolvido' });
  if (loan.renewCount >= MAX_RENEWS)
    return res.status(400).json({ error: 'Renovação máxima atingida' });

  const base = loan.dueDate || new Date();
  const newDue = new Date(base.getTime() + addDays * 24 * 60 * 60 * 1000);

  loan.status = 'RENOVADO';
  loan.dueDate = newDue;
  loan.renewCount += 1;

  // Se havia pedido pendente, considera como aprovado nesta ação direta
  if (loan.renewalRequested) {
    loan.renewalRequested = false;
    loan.renewalReviewedAt = new Date();
    loan.renewalDeniedReason = '';
  }

  await loan.save();

  await Notification.create({
    userId: loan.userId,
    title: 'Empréstimo renovado',
    body: `Novo vencimento: ${newDue.toLocaleDateString()}`
  });

  res.json(loan);
});

// ============================================================================
// RF13 — Fluxo de RENOVAÇÃO COM APROVAÇÃO DO ADMIN
// ============================================================================

// -----------------------------------------------------------------------------
// NOVO: Listar solicitações de renovação pendentes
// -----------------------------------------------------------------------------
router.get('/renew-requests', requireAuth, requireAdmin, async (req, res) => {
  try {
    const items = await Loan.find({ renewalRequested: true })
      .sort({ renewalRequestedAt: -1 })
      .populate('bookId', 'title author coverUrl')
      .populate('userId', 'name email');
    res.json(items);
  } catch (e) {
    res.status(500).json({ error: 'Falha ao listar renovações pendentes' });
  }
});

// -----------------------------------------------------------------------------
// Aprovar solicitação de renovação
// -----------------------------------------------------------------------------
router.post('/:id/renew-approve', requireAuth, requireAdmin, async (req, res) => {
  try {
    const { id } = req.params;
    const addDaysBody = Number(req.body?.days ?? NaN);

    const loan = await Loan.findById(id);
    if (!loan) return res.status(404).json({ error: 'Empréstimo não encontrado' });

    if (!loan.renewalRequested)
      return res.status(400).json({ error: 'Não há solicitação de renovação pendente' });

    if (!['APROVADO', 'RENOVADO'].includes(loan.status))
      return res.status(400).json({ error: 'Empréstimo não está ativo' });

    if (loan.returnedAt)
      return res.status(400).json({ error: 'Empréstimo já devolvido' });

    if (loan.renewCount >= MAX_RENEWS)
      return res.status(400).json({ error: 'Renovação máxima atingida' });

    const addDays = !isNaN(addDaysBody) && addDaysBody > 0
      ? addDaysBody
      : (loan.renewalAddDays || DEFAULT_RENEW_DAYS);

    const base = loan.dueDate || new Date();
    const newDue = new Date(base.getTime() + addDays * 24 * 60 * 60 * 1000);

    loan.status = 'RENOVADO';
    loan.dueDate = newDue;
    loan.renewCount += 1;
    loan.renewalRequested = false;
    loan.renewalReviewedAt = new Date();
    loan.renewalDeniedReason = '';
    await loan.save();

    await Notification.create({
      userId: loan.userId,
      title: 'Renovação aprovada',
      body: `Seu empréstimo foi renovado por ${addDays} dia(s). Novo vencimento: ${newDue.toLocaleDateString()}`
    });

    res.json(loan);
  } catch (e) {
    res.status(500).json({ error: 'Falha ao aprovar renovação' });
  }
});

// -----------------------------------------------------------------------------
// Negar solicitação de renovação
// -----------------------------------------------------------------------------
router.post('/:id/renew-deny', requireAuth, requireAdmin, async (req, res) => {
  try {
    const { id } = req.params;
    const reason = String(req.body?.reason ?? 'Solicitação não aprovada');

    const loan = await Loan.findById(id);
    if (!loan) return res.status(404).json({ error: 'Empréstimo não encontrado' });

    if (!loan.renewalRequested)
      return res.status(400).json({ error: 'Não há solicitação de renovação pendente' });

    loan.renewalRequested = false;
    loan.renewalReviewedAt = new Date();
    loan.renewalDeniedReason = reason;
    await loan.save();

    await Notification.create({
      userId: loan.userId,
      title: 'Renovação negada',
      body: `Seu pedido de renovação foi negado. Motivo: ${reason}`
    });

    res.json({ message: 'Renovação negada', loan });
  } catch (e) {
    res.status(500).json({ error: 'Falha ao negar renovação' });
  }
});

export default router;
