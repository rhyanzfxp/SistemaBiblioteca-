import express from "express";
import { GoogleGenerativeAI } from "@google/generative-ai";

const router = express.Router();
const genAI = new GoogleGenerativeAI(process.env.GOOGLE_API_KEY);
const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

router.post("/chat", async (req, res) => {
  try {
    const { question } = req.body;

    if (!question) {
      return res.status(400).json({ error: "Pergunta não informada." });
    }

    // 🔧 Prompt adaptado à documentação do projeto Unifor Library
    const prompt = `
    Você é o assistente virtual **BIBLLE**, chatbot oficial da Biblioteca da Universidade de Fortaleza.
    Seu papel é ajudar os alunos e bibliotecários a usarem o sistema da biblioteca de forma simples e rápida.

    Baseie suas respostas **exclusivamente** nas funcionalidades descritas na documentação do projeto Unifor Library:
    - O acervo da biblioteca permite pesquisar livros por título, autor ou tema.
    - Mostra localização de livros em um mapa 2D interativo, com zoom e movimento.
    - Permite salvar livros favoritos, solicitar empréstimos, renovar livros, e ver prazos e notificações.
    - O usuário pode visualizar detalhes do livro (autor, edição, disponibilidade, setor/estante).
    - A renovação só é possível se o livro não tiver reserva ativa e respeitar o limite de renovações definido pela biblioteca.
    - O bibliotecário pode aprovar, recusar e registrar devoluções.
    - O app envia notificações sobre prazos, renovações e mensagens do bibliotecário.
    - A interface é acessível e compatível com leitores de tela e Libras.

    Contexto:
    - Usuário: aluno da universidade.
    - Caso o usuário pergunte algo fora do escopo da biblioteca, responda educadamente que sua função é responder apenas sobre o uso do sistema Unifor Library.

    Formato da resposta:
    - Seja curto, direto e contextualizado com o sistema.
    - Quando possível, cite as telas e funções reais (ex: “vá em Meus Empréstimos > Renovar” ou “verifique o mapa 2D na aba Mapa”).

    Pergunta do usuário:
    ${question}
    `;

    const result = await model.generateContent(prompt);
    const response = await result.response;
    const text = response.text();

    res.json({ reply: text });
  } catch (error) {
    console.error("Erro ao gerar resposta:", error);
    res.status(500).json({ error: "Erro ao gerar resposta do chatbot" });
  }
});

export default router;
