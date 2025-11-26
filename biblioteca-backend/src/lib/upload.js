import multer from 'multer';
import path from 'path';
import { v4 as uuid } from 'uuid';

const AVATAR_DIR = path.resolve('uploads', 'avatars');
const COVER_DIR = path.resolve('uploads', 'covers');

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    let dir = AVATAR_DIR;
    if (req.originalUrl.includes('/books/cover')) {
      dir = COVER_DIR;
    }
    cb(null, dir);
  },
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

export const coverUpload = multer({
  storage,
  fileFilter,
  limits: { fileSize: 5 * 1024 * 1024 }
});

export const AVATAR_DIR_ABS = AVATAR_DIR;
export const COVER_DIR_ABS = COVER_DIR;