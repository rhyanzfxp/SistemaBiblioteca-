import mongoose from 'mongoose';
const { Schema } = mongoose;

const LoanSchema = new Schema({
  userId: { type: Schema.Types.ObjectId, ref: 'User', index: true },
  bookId: { type: Schema.Types.ObjectId, ref: 'Book' },
  status: { type: String, enum: ['PENDENTE','APROVADO','NEGADO','DEVOLVIDO','RENOVADO'], default: 'PENDENTE' },
  reason: String,
  requestedAt: { type: Date, default: Date.now },
  approvedAt: Date,
  startDate: Date,
  dueDate: Date,
  returnedAt: Date,
  renewCount: { type: Number, default: 0 }
});

export default mongoose.model('Loan', LoanSchema);
