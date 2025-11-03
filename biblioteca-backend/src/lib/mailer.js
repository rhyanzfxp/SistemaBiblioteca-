// src/lib/mailer.js
import nodemailer from 'nodemailer';

export async function makeTransport() {

  if ((process.env.SMTP_HOST || '').includes('gmail') || process.env.SMTP_USER?.endsWith('@gmail.com')) {
    return nodemailer.createTransport({
      service: 'gmail',
      auth: { user: process.env.SMTP_USER, pass: process.env.SMTP_PASS },
    });
  }


  return nodemailer.createTransport({
    host: process.env.SMTP_HOST,
    port: Number(process.env.SMTP_PORT || 587),
    secure: false,
    auth: { user: process.env.SMTP_USER, pass: process.env.SMTP_PASS },
  });
}

export async function sendMail({ to, subject, html }) {
  const transporter = await makeTransport();
  const info = await transporter.sendMail({
    from: process.env.SMTP_USER,
    to,
    subject,
    html,
  });

  console.log('MAIL SENT:', { messageId: info.messageId, accepted: info.accepted, rejected: info.rejected });
  return info;
}
