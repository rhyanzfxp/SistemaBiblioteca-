import mongoose from 'mongoose';
const { Schema } = mongoose;

const LoanSchema = new Schema({
  userId: { type: Schema.Types.ObjectId, ref: 'User', index: true },
  bookId: { type: Schema.Types.ObjectId, ref: 'Book' },

  status: {
    type: String,
    enum: ['PENDENTE', 'APROVADO', 'NEGADO', 'DEVOLVIDO', 'RENOVADO'],
    default: 'PENDENTE'
  },

  reason: String,
  requestedAt: { type: Date, default: Date.now },
  approvedAt: Date,
  startDate: Date,
  dueDate: Date,
  returnedAt: Date,

  // Renovação
  renewCount: { type: Number, default: 0 },
  renewalRequested: { type: Boolean, default: false },
  renewalAddDays: { type: Number, default: 0 },
  renewalReason: { type: String, default: '' },
  renewalRequestedAt: Date,
  renewalReviewedAt: Date,
  renewalDeniedReason: { type: String, default: '' }
});

// Índices recomendados para consultas de ativos/renovações
LoanSchema.index({ status: 1, userId: 1, dueDate: 1 });

export default mongoose.model('Loan', LoanSchema);
