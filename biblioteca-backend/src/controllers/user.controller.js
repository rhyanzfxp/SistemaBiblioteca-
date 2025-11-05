import bcrypt from 'bcryptjs';
import User from '../models/User.js';

/**
 * GET /users/me
 * Dados do próprio usuário (sem passwordHash)
 */
export const getMe = async (req, res) => {
  const u = await User.findById(req.user.id).select('-passwordHash');
  if (!u) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(u);
};

/**
 * PATCH /users/me
 * Atualiza nome, email, photoUrl (o próprio usuário)
 * Não permite alterar role/active por aqui.
 */
export const updateMe = async (req, res) => {
  const data = {};
  if (typeof req.body.name === 'string') data.name = req.body.name.trim();
  if (typeof req.body.email === 'string') data.email = req.body.email.trim().toLowerCase();
  if (typeof req.body.photoUrl === 'string') data.photoUrl = req.body.photoUrl.trim();

  // Evita alterações indevidas
  delete data.role;
  delete data.active;
  delete data.passwordHash;

  const u = await User.findByIdAndUpdate(
    req.user.id,
    data,
    { new: true, runValidators: true }
  ).select('-passwordHash');

  if (!u) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(u);
};

/**
 * GET /users/me/accessibility
 * Retorna preferências de acessibilidade
 */
export const getMyAccessibility = async (req, res) => {
  const u = await User.findById(req.user.id).select('accessibility');
  if (!u) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(u.accessibility);
};

/**
 * PATCH /users/me/accessibility
 * Atualiza preferências de acessibilidade (fontSize, contrast, voiceAssist, libras)
 */
export const updateMyAccessibility = async (req, res) => {
  const { fontSize, contrast, voiceAssist, libras } = req.body;

  const u = await User.findByIdAndUpdate(
    req.user.id,
    { accessibility: { fontSize, contrast, voiceAssist, libras } },
    { new: true, runValidators: true }
  ).select('accessibility');

  if (!u) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(u.accessibility);
};

/**
 * DELETE /users/me
 * Auto-exclusão de conta (RNF04.2)
 */
export const deleteMe = async (req, res) => {
  await User.findByIdAndDelete(req.user.id);
  res.json({ ok: true });
};

/**
 * ------- ADMIN A PARTIR DAQUI -------
 * As funções abaixo espelham o que já existe nas rotas admins de users.js,
 * caso deseje unificar tudo no controller no futuro.
 */

export const adminListUsers = async (req, res) => {
  const limit = Math.min(parseInt(req.query.limit || '20'), 100);
  const page = Math.max(parseInt(req.query.page || '1'), 1);
  const skip = (page - 1) * limit;

  const q = (req.query.q || '').trim();
  const filter = q
    ? { $or: [{ name: { $regex: q, $options: 'i' } }, { email: { $regex: q, $options: 'i' } }] }
    : {};

  const [total, items] = await Promise.all([
    User.countDocuments(filter),
    User.find(filter, { passwordHash: 0 }).skip(skip).limit(limit).sort({ createdAt: -1 })
  ]);

  res.json({ items, total, page, pages: Math.max(Math.ceil(total / limit), 1) });
};

export const adminGetUser = async (req, res) => {
  const u = await User.findById(req.params.id).select('-passwordHash');
  if (!u) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(u);
};

export const adminPatchUser = async (req, res) => {
  const data = {};
  if (typeof req.body.name === 'string') data.name = req.body.name.trim();
  if (typeof req.body.email === 'string') data.email = req.body.email.trim().toLowerCase();
  if (req.body.role && ['user', 'admin'].includes(req.body.role)) data.role = req.body.role;

  if (req.body.password) data.passwordHash = bcrypt.hashSync(req.body.password, 10);

  const u = await User.findByIdAndUpdate(
    req.params.id,
    data,
    { new: true, runValidators: true }
  ).select('-passwordHash');

  if (!u) return res.status(404).json({ error: 'Usuário não encontrado' });
  res.json(u);
};

export const adminPatchStatus = async (req, res) => {
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
};

export const adminDeleteUser = async (req, res) => {
  await User.findByIdAndDelete(req.params.id);
  res.json({ ok: true });
};
