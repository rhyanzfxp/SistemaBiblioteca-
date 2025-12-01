import mongoose from 'mongoose';

const BookSchema = new mongoose.Schema({
  title: { type: String, required: true },
  author: { type: String, required: true },
  isbn: { type: String },
  copiesTotal: { type: Number, default: 1 },
  copiesAvailable: { type: Number, default: 1 },
  tags: [{ type: String }],
  coverUrl: { type: String },
  description: { type: String },
  sector: { type: String },
  shelfCode: { type: String },
  year: { type: Number },
  edition: { type: String }
}, { timestamps: true });

export default mongoose.model('Book', BookSchema);