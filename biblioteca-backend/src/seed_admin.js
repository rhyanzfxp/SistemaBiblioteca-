import 'dotenv/config';
import mongoose from 'mongoose';
import bcrypt from 'bcryptjs';
import User from './models/User.js';

async function run(){
  await mongoose.connect(process.env.MONGODB_URI);
  const email = process.env.SEED_ADMIN_EMAIL || 'admin@biblioteca.com';
  const name = process.env.SEED_ADMIN_NAME || 'Admin Biblioteca';
  const password = process.env.SEED_ADMIN_PASSWORD || '123456';
  const passwordHash = bcrypt.hashSync(password, 10);

  const exist = await User.findOne({ email });
  if (exist){
    exist.name = name;
    exist.role = 'admin';
    exist.active = true;
    if (process.env.SEED_RESET_PASSWORD === 'true') {
      exist.passwordHash = passwordHash;
    }
    await exist.save();
    console.log('Admin atualizado:', email);
  } else {
    await User.create({ name, email, passwordHash, role:'admin', active:true });
    console.log('Admin criado:', email);
  }
  await mongoose.disconnect();
}

run().then(()=>{ console.log(' seed:admin OK'); process.exit(0); }).catch(e=>{ console.error(e); process.exit(1); });
