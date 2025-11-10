// src/lib/mailer.js
import nodemailer from 'nodemailer';

function buildTransportOptions() {
  const host = process.env.SMTP_HOST || 'smtp.gmail.com';
  const port = Number(process.env.SMTP_PORT || 465);
  const secure = port === 465;

  return {
    host,
    port,
    secure,
    auth: {
      user: process.env.SMTP_USER,
      pass: process.env.SMTP_PASS,
    },
  };
}

export async function makeTransport() {
  const transporter = nodemailer.createTransport(buildTransportOptions());


  try {
    await transporter.verify();
    console.log('[MAILER] SMTP ok em', process.env.SMTP_HOST, 'porta', process.env.SMTP_PORT);
  } catch (e) {
    console.error('[MAILER] Falha no verify()', e);
  }

  return transporter;
}

export async function sendMail({ to, subject, html, text }) {
  const transporter = await makeTransport();
  const info = await transporter.sendMail({
    from: process.env.SMTP_FROM || process.env.SMTP_USER,
    to,
    subject,
    text,
    html,
  });

  console.log('MAIL SENT:', {
    messageId: info.messageId,
    accepted: info.accepted,
    rejected: info.rejected,
  });

  return info;
}
