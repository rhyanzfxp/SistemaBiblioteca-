/**
 * Middleware para validar força da senha conforme RF02.3
 * A senha deve ter ao menos 8 caracteres, incluindo um número e uma letra maiúscula
 */
export function validatePasswordStrength(req, res, next) {
  const { password } = req.body || {};
  
  if (!password) {
    return next(); // Deixa a validação de campo obrigatório para outro lugar
  }

  // Verifica se tem pelo menos 8 caracteres
  if (password.length < 8) {
    return res.status(400).json({ 
      error: 'A senha deve ter pelo menos 8 caracteres, incluindo um número e uma letra maiúscula' 
    });
  }

  // Verifica se tem pelo menos um número
  if (!/\d/.test(password)) {
    return res.status(400).json({ 
      error: 'A senha deve ter pelo menos 8 caracteres, incluindo um número e uma letra maiúscula' 
    });
  }

  // Verifica se tem pelo menos uma letra maiúscula
  if (!/[A-Z]/.test(password)) {
    return res.status(400).json({ 
      error: 'A senha deve ter pelo menos 8 caracteres, incluindo um número e uma letra maiúscula' 
    });
  }

  next();
}
