package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.R
import java.time.LocalDate
import java.util.UUID

class LoanRepository(private val context: Context) {

    // Mock em memória — troque por banco/API quando for integrar
    private val memory = mutableListOf<Loan>()

    init {
        // Nada aqui: vamos semear sob demanda para o usuário em LoansFragment
    }

    /** Lista empréstimos do usuário (case-insensitive) */
    fun listForUser(email: String): List<Loan> =
        memory.filter { it.userEmail.equals(email, ignoreCase = true) }

    /** Cria exemplos para o e-mail informado (ativos, atrasados e histórico). */
    fun seedFor(email: String) {
        // Mantém os empréstimos de outros usuários e substitui apenas os deste usuário.
        memory.removeAll { it.userEmail.equals(email, ignoreCase = true) }

        val today = LocalDate.now()

        // Ativos
        memory += Loan(
            id = UUID.randomUUID().toString(),
            userEmail = email,
            bookId = "clean_code",
            bookTitle = "Clean Code",
            coverRes = R.drawable.ic_book_placeholder,
            startDate = today.minusDays(7),
            dueDate   = today.plusDays(5)
        )
        memory += Loan(
            id = UUID.randomUUID().toString(),
            userEmail = email,
            bookId = "refactoring",
            bookTitle = "Refactoring",
            coverRes = R.drawable.ic_book_placeholder,
            startDate = today.minusDays(3),
            dueDate   = today.plusDays(10)
        )

        // Atrasados
        memory += Loan(
            id = UUID.randomUUID().toString(),
            userEmail = email,
            bookId = "algorithms",
            bookTitle = "Algoritmos – Estruturas de Dados e Análise",
            coverRes = R.drawable.ic_book_placeholder,
            startDate = today.minusDays(25),
            dueDate   = today.minusDays(2)
        )
        memory += Loan(
            id = UUID.randomUUID().toString(),
            userEmail = email,
            bookId = "java_complete",
            bookTitle = "Java: A Beginner’s Guide",
            coverRes = R.drawable.ic_book_placeholder,
            startDate = today.minusDays(45),
            dueDate   = today.minusDays(10),
            renewCount = 2
        )

        // Histórico
        memory += Loan(
            id = UUID.randomUUID().toString(),
            userEmail = email,
            bookId = "design_patterns",
            bookTitle = "Padrões de Projeto",
            coverRes = R.drawable.ic_book_placeholder,
            startDate = today.minusDays(60),
            dueDate   = today.minusDays(45),
            returnedDate = today.minusDays(44)
        )
        memory += Loan(
            id = UUID.randomUUID().toString(),
            userEmail = email,
            bookId = "eng_software",
            bookTitle = "Engenharia de Software – Sommerville",
            coverRes = R.drawable.ic_book_placeholder,
            startDate = today.minusDays(90),
            dueDate   = today.minusDays(75),
            returnedDate = today.minusDays(70)
        )
    }

    fun renew(loanId: String, days: Long = 7): Boolean {
        val l = memory.find { it.id == loanId } ?: return false
        if (l.isReturned) return false
        if (l.renewCount >= 2) return false
        l.dueDate = l.dueDate.plusDays(days)
        l.renewCount += 1
        return true
    }

    fun returnBook(loanId: String): Boolean {
        val l = memory.find { it.id == loanId } ?: return false
        if (l.isReturned) return false
        l.returnedDate = LocalDate.now()
        return true
    }
}
