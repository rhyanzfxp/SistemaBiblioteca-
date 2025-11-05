import mongoose from 'mongoose';
const { Schema } = mongoose;

const UserSchema = new Schema({
  name:   { type: String, required: true },
  email:  { type: String, required: true, unique: true, index: true },
  passwordHash: { type: String, required: true },
  role:   { type: String, enum: ['user','admin'], default: 'user' },
  active: { type: Boolean, default: true },


  favorites: [{ type: Schema.Types.ObjectId, ref: 'Book', default: [] }],

  resetPasswordToken: String,
  resetPasswordExpires: Date
}, { timestamps: true });

export default mongoose.model('User', UserSchema);
