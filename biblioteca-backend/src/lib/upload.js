import multer from 'multer';
import path from 'path';
import { v4 as uuid } from 'uuid';

const AVATAR_DIR = path.resolve('uploads', 'avatars');

const storage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, AVATAR_DIR),
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname || '').toLowerCase();
    cb(null, `${uuid()}${ext || '.jpg'}`);
  }
});

function fileFilter(_req, file, cb) {
  const ok = ['image/jpeg','image/png','image/webp','image/jpg'].includes(file.mimetype);
  if (!ok) return cb(new Error('Formato inválido (use JPG, PNG ou WEBP)'));
  cb(null, true);
}

export const avatarUpload = multer({
  storage,
  fileFilter,
  limits: { fileSize: 5 * 1024 * 1024 }
});

export const AVATAR_DIR_ABS = AVATAR_DIR;
