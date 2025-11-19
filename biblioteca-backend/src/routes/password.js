import express from 'express';
import crypto from 'crypto';
import bcrypt from 'bcryptjs';
import User from '../models/User.js';
import { sendMail } from '../lib/mailer.js';

const router = express.Router();

/* ==============================
   Templates de e-mail (HTML)
   ============================== */

function renderResetEmail({ name = 'Usuário', link, brand = 'Unifor Library' }) {
  const bg = '#0B1020';
  const card = '#141A2E';
  const text = '#E6EAF5';
  const muted = '#B3B9CC';
  const accent = '#7EE787';
  const preheader = 'Use o botão para redefinir sua senha. O link expira em 1 hora.';

  return `<!doctype html>
<html lang="pt-br">
<head>
  <meta charset="utf-8"><meta name="viewport" content="width=device-width">
  <title>${brand} • Redefinição de senha</title>
</head>
<body style="margin:0;padding:0;background:${bg};">
  <div style="display:none;opacity:0;height:0;width:0;overflow:hidden;color:transparent;">${preheader}</div>
  <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:${bg};">
    <tr><td align="center" style="padding:24px;">
      <table role="presentation" width="600" style="width:600px;max-width:600px;background:${card};border-radius:16px;overflow:hidden;">
        <tr><td align="center" style="padding:24px 24px 8px;"></td></tr>
        <tr><td align="center" style="padding:0 24px 12px;">
          <div style="font-family:Arial,Helvetica,sans-serif;font-size:18px;line-height:24px;color:${muted};">${brand}</div>
        </td></tr>
        <tr><td style="padding:8px 24px 24px;">
          <h1 style="margin:0 0 12px 0;font-family:Arial,Helvetica,sans-serif;font-size:22px;line-height:28px;color:${text};">Redefinição de senha</h1>
          <p style="margin:0 0 12px 0;font-family:Arial,Helvetica,sans-serif;font-size:15px;line-height:22px;color:${muted};">
            Olá, <strong style="color:${text};">${name}</strong>! Recebemos uma solicitação para redefinir sua senha.
          </p>
          <p style="margin:0 0 18px 0;font-family:Arial,Helvetica,sans-serif;font-size:15px;line-height:22px;color:${muted};">
            Clique no botão abaixo para continuar. Este link é válido por <strong style="color:${text};">1 hora</strong>.
          </p>
          <table role="presentation" cellpadding="0" cellspacing="0" border="0" style="margin:0 auto 16px;">
            <tr><td align="center" bgcolor="${accent}" style="border-radius:10px;">
              <a href="${link}" style="display:inline-block;padding:14px 22px;font-family:Arial,Helvetica,sans-serif;font-size:15px;font-weight:bold;color:#0B1020;text-decoration:none;">
                Redefinir senha
              </a>
            </td></tr>
          </table>
          <p style="margin:0 0 8px 0;font-family:Arial,Helvetica,sans-serif;font-size:12px;line-height:18px;color:${muted};">
            Se o botão não funcionar, copie e cole esta URL no navegador:
          </p>
          <p style="margin:0 0 18px 0;word-break:break-all;font-family:Arial,Helvetica,sans-serif;font-size:12px;line-height:18px;color:${accent};">
            <a href="${link}" style="color:${accent};text-decoration:none;">${link}</a>
          </p>
          <hr style="border:none;border-top:1px solid #1F2742;margin:10px 0 14px 0;">
          <p style="margin:0;font-family:Arial,Helvetica,sans-serif;font-size:12px;line-height:18px;color:${muted};">
            Não foi você? Ignore este e-mail — nenhuma alteração será feita.
          </p>
        </td></tr>
      </table>
      <table role="presentation" width="600" style="width:600px;max-width:600px;margin-top:10px;">
        <tr><td align="center" style="padding:8px 16px;">
          <p style="margin:0;font-family:Arial,Helvetica,sans-serif;font-size:11px;line-height:16px;color:${muted};">
            © ${new Date().getFullYear()} ${brand}. Todos os direitos reservados.
          </p>
        </td></tr>
      </table>
    </td></tr>
  </table>
</body>
</html>`;
}

function renderPasswordChangedEmail({
  name = 'Usuário',
  brand = 'Unifor Library',
  security = {},
}) {
  const bg = '#0B1020';
  const card = '#141A2E';
  const text = '#E6EAF5';
  const muted = '#B3B9CC';
  const accent = '#7EE787';
  const preheader = 'Sua senha foi alterada com sucesso. Se não foi você, revise sua conta imediatamente.';

  const rows = [
    security.time ? `<tr><td style="padding:4px 0;color:${muted};">Data/Hora: <span style="color:${text};">${security.time}</span></td></tr>` : '',
    security.ip ? `<tr><td style="padding:4px 0;color:${muted};">IP: <span style="color:${text};">${security.ip}</span></td></tr>` : '',
    security.ua ? `<tr><td style="padding:4px 0;color:${muted};">Dispositivo/Navegador: <span style="color:${text};">${security.ua}</span></td></tr>` : '',
  ].join('');

  return `<!doctype html>
<html lang="pt-br">
<head>
  <meta charset="utf-8"><meta name="viewport" content="width=device-width">
  <title>${brand} • Senha alterada</title>
</head>
<body style="margin:0;padding:0;background:${bg};">
  <div style="display:none;opacity:0;height:0;width:0;overflow:hidden;color:transparent;">${preheader}</div>
  <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:${bg};">
    <tr><td align="center" style="padding:24px;">
      <table role="presentation" width="600" style="width:600px;max-width:600px;background:${card};border-radius:16px;overflow:hidden;">
        <tr><td align="center" style="padding:24px 24px 8px;"></td></tr>
        <tr><td align="center" style="padding:0 24px 12px;">
          <div style="font-family:Arial,Helvetica,sans-serif;font-size:18px;line-height:24px;color:${muted};">${brand}</div>
        </td></tr>
        <tr><td style="padding:8px 24px 24px;">
          <h1 style="margin:0 0 12px 0;font-family:Arial,Helvetica,sans-serif;font-size:22px;line-height:28px;color:${text};">Senha alterada com sucesso</h1>
          <p style="margin:0 0 12px 0;font-family:Arial,Helvetica,sans-serif;font-size:15px;line-height:22px;color:${muted};">
            Olá, <strong style="color:${text};">${name}</strong>! A senha da sua conta foi alterada com sucesso.
          </p>
          ${rows ? `<table role="presentation" width="100%" style="margin:8px 0 12px;">${rows}</table>` : ''}
          <p style="margin:0 0 16px 0;font-family:Arial,Helvetica,sans-serif;font-size:14px;line-height:22px;color:${muted};">
            Se não foi você, recomendamos <strong style="color:${text};">redefinir a senha novamente</strong> e revisar sessões ativas.
          </p>
          <table role="presentation" cellpadding="0" cellspacing="0" border="0" style="margin:0 auto 8px;">
            <tr><td align="center" bgcolor="${accent}" style="border-radius:10px;">
              <a href="${process.env.APP_URL || '#'}" style="display:inline-block;padding:12px 20px;font-family:Arial,Helvetica,sans-serif;font-size:14px;font-weight:bold;color:#0B1020;text-decoration:none;">
                Ir para o sistema
              </a>
            </td></tr>
          </table>
          <hr style="border:none;border-top:1px solid #1F2742;margin:14px 0;">
          <p style="margin:0;font-family:Arial,Helvetica,sans-serif;font-size:12px;line-height:18px;color:${muted};">
            Dica: ative a verificação em duas etapas e mantenha seu e-mail atualizado.
          </p>
        </td></tr>
      </table>
      <table role="presentation" width="600" style="width:600px;max-width:600px;margin-top:10px;">
        <tr><td align="center" style="padding:8px 16px;">
          <p style="margin:0;font-family:Arial,Helvetica,sans-serif;font-size:11px;line-height:16px;color:${muted};">
            © ${new Date().getFullYear()} ${brand}. Todos os direitos reservados.
          </p>
        </td></tr>
      </table>
    </td></tr>
  </table>
</body>
</html>`;
}

/* ==============================
   POST /auth/forgot
   ============================== */

router.post('/forgot', async (req, res) => {
  try {
    const { email } = req.body || {};
    if (!email) return res.status(400).json({ error: 'E-mail é obrigatório' });

    const user = await User.findOne({ email: String(email).toLowerCase().trim() });

    // Responde ok mesmo se não existir (pra não vazar se o e-mail está cadastrado)
    if (!user) return res.json({ ok: true });

    const rawToken = crypto.randomBytes(32).toString('hex');
    const hash = crypto.createHash('sha256').update(rawToken).digest('hex');

    user.resetPasswordToken = hash;
    user.resetPasswordExpires = new Date(Date.now() + 1000 * 60 * 60); // 1h
    await user.save();

    const base =
      process.env.RESET_URL ||
      (process.env.APP_URL
        ? process.env.APP_URL + '/auth/reset'
        : 'http://localhost:8080/auth/reset');

    const link = `${base}/${rawToken}`;

    const html = renderResetEmail({
      name: user.name || 'Usuário',
      link,
      brand: process.env.APP_BRAND || 'Unifor Library',
    });

    const info = await sendMail({
      to: user.email,
      subject: 'Redefinição de senha',
      html,
    });
    console.log('FORGOT OK', { accepted: info.accepted, rejected: info.rejected });

    res.json({ ok: true });
  } catch (e) {
    console.error('FORGOT ERROR', e);
    res.status(500).json({ error: 'Falha ao enviar e-mail' });
  }
});

/* ==============================
   GET /auth/reset/:token
   Página de redefinição (HTML)
   ============================== */

router.get('/reset/:token', async (req, res) => {
  const { token } = req.params;

  // Aqui não validamos hash ainda, só mostramos o form.
  // A validação real acontece no POST /reset.
  res.setHeader('Content-Type', 'text/html; charset=utf-8');
  res.send(`<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<title>Redefinir Senha</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  body {
    background: #050816;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    margin: 0;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
    color: #f9fafb;
  }
  .card {
    background: #111827;
    padding: 32px 32px;
    width: 360px;
    border-radius: 18px;
    box-shadow: 0 24px 60px rgba(0, 0, 0, 0.6);
  }
  h2 {
    margin: 0 0 16px;
    text-align: center;
    font-size: 22px;
  }
  label {
    font-size: 14px;
    color: #e5e7eb;
  }
  input {
    width: 100%;
    padding: 11px 12px;
    margin-top: 8px;
    border-radius: 10px;
    border: 1px solid #374151;
    background: #020617;
    color: #f9fafb;
    font-size: 14px;
     box-sizing: border-box;
  }
  input:focus {
    outline: none;
    border-color: #7c3aed;
    box-shadow: 0 0 0 1px #7c3aed;
  }
  button {
    width: 100%;
    padding: 12px;
    margin-top: 22px;
    border: none;
    border-radius: 999px;
    background: #7c3aed;
    color: white;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
  }
  button:hover {
    background: #6d28d9;
  }
  .hint {
    font-size: 12px;
    color: #9ca3af;
    margin-top: 8px;
  }
</style>
</head>
<body>
  <div class="card">
    <h2>Redefinir senha</h2>
    <form method="POST" action="/auth/reset">
      <input type="hidden" name="token" value="${token}">
      <label>Nova senha</label>
      <input type="password" name="password" required />
      <p class="hint">Após confirmar, você poderá usar esta senha no app Unifor Library.</p>
      <button type="submit">Trocar senha</button>
    </form>
  </div>
</body>
</html>`);
});

/* ==============================
   POST /auth/reset
   Faz a troca da senha e exibe
   página de sucesso/erro
   ============================== */

router.post('/reset', async (req, res) => {
  try {
    const { token, password } = req.body || {};
    if (!token || !password) {
      res.setHeader('Content-Type', 'text/html; charset=utf-8');
      return res.status(400).send(`<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<title>Erro</title>
<style>
  body { background:#050816; display:flex; justify-content:center; align-items:center; height:100vh; margin:0; font-family:system-ui, sans-serif; color:#f9fafb; }
  .card { background:#111827; padding:32px 28px; width:360px; border-radius:18px; box-shadow:0 24px 60px rgba(0,0,0,0.6); text-align:center; }
  h2 { color:#f97373; margin-bottom:10px; }
</style>
</head>
<body>
  <div class="card">
    <h2>Dados inválidos</h2>
    <p>Token e nova senha são obrigatórios. Tente novamente a partir do link enviado por e-mail.</p>
  </div>
</body>
</html>`);
    }

    const hash = crypto.createHash('sha256').update(token).digest('hex');
    const user = await User.findOne({
      resetPasswordToken: hash,
      resetPasswordExpires: { $gt: new Date() },
    });

    if (!user) {
      res.setHeader('Content-Type', 'text/html; charset=utf-8');
      return res.status(400).send(`<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<title>Token inválido</title>
<style>
  body { background:#050816; display:flex; justify-content:center; align-items:center; height:100vh; margin:0; font-family:system-ui, sans-serif; color:#f9fafb; }
  .card { background:#111827; padding:32px 28px; width:360px; border-radius:18px; box-shadow:0 24px 60px rgba(0,0,0,0.6); text-align:center; }
  h2 { color:#f97373; margin-bottom:10px; }
</style>
</head>
<body>
  <div class="card">
    <h2>Token inválido ou expirado</h2>
    <p>Solicite uma nova redefinição de senha pelo aplicativo.</p>
  </div>
</body>
</html>`);
    }

    const salt = await bcrypt.genSalt(10);
    user.passwordHash = await bcrypt.hash(password, salt);
    user.resetPasswordToken = null;
    user.resetPasswordExpires = null;
    await user.save();

    const html = renderPasswordChangedEmail({
      name: user.name || 'Usuário',
      brand: process.env.APP_BRAND || 'Unifor Library',
      security: {
        time: new Date().toLocaleString('pt-BR', { timeZone: 'America/Fortaleza' }),
        ip:
          req.headers['x-forwarded-for']?.split(',')[0]?.trim() ||
          req.socket?.remoteAddress ||
          'indisponível',
        ua: req.headers['user-agent'] || 'indisponível',
      },
    });
    const info = await sendMail({
      to: user.email,
      subject: 'Sua senha foi alterada',
      html,
    });
    console.log('RESET MAIL', { accepted: info.accepted, rejected: info.rejected });

    // Página de sucesso
    res.setHeader('Content-Type', 'text/html; charset=utf-8');
    res.send(`<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<title>Senha redefinida</title>
<style>
  body { background:#050816; display:flex; justify-content:center; align-items:center; height:100vh; margin:0; font-family:system-ui, sans-serif; color:#f9fafb; }
  .card { background:#111827; padding:36px 30px; width:360px; border-radius:18px; box-shadow:0 24px 60px rgba(0,0,0,0.6); text-align:center; }
  h2 { color:#4ade80; margin-bottom:10px; }
  p { font-size:14px; color:#e5e7eb; }
</style>
</head>
<body>
  <div class="card">
    <h2>Senha redefinida!</h2>
    <p>Agora você já pode fechar esta página e fazer login no app Unifor Library com a nova senha.</p>
  </div>
</body>
</html>`);
  } catch (e) {
    console.error('RESET ERROR', e);
    res.setHeader('Content-Type', 'text/html; charset=utf-8');
    res.status(500).send(`<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<title>Erro</title>
<style>
  body { background:#050816; display:flex; justify-content:center; align-items:center; height:100vh; margin:0; font-family:system-ui, sans-serif; color:#f9fafb; }
  .card { background:#111827; padding:32px 28px; width:360px; border-radius:18px; box-shadow:0 24px 60px rgba(0,0,0,0.6); text-align:center; }
  h2 { color:#f97373; margin-bottom:10px; }
</style>
</head>
<body>
  <div class="card">
    <h2>Erro ao redefinir</h2>
    <p>Ocorreu um erro inesperado ao redefinir sua senha. Tente novamente em alguns instantes.</p>
  </div>
</body>
</html>`);
  }
});

export default router;
