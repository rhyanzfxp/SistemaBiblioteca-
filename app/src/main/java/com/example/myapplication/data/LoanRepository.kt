package com.example.myapplication.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.myapplication.R
import java.time.LocalDate
import java.util.UUID

class LoanRepository(private val context: Context) {

    // ===== Memória compartilhada entre TODAS as instâncias =====
    companion object {
        private val memory = mutableListOf<Loan>()
    }

    /** Listagens */
    fun listAll(): List<Loan> = memory.toList()
    fun listSolicitados(): List<Loan> = memory.filter { it.status == "SOLICITADO" }
    fun listForUser(email: String): List<Loan> =
        memory.filter { it.userEmail.equals(email, ignoreCase = true) }

    /** Busca direta por ID (usado na tela de revisão) */
    fun getById(id: String): Loan? = memory.firstOrNull { it.id == id }

    /** Semeia dados de exemplo */
    @RequiresApi(Build.VERSION_CODES.O)
    fun seedFor(email: String) {
        memory.removeAll { it.userEmail.equals(email, ignoreCase = true) }
        val today = LocalDate.now()

        // SOLICITADO
        memory += Loan(
            id = UUID.randomUUID().toString(),
            userEmail = email,
            bookId = "clean_code",
            bookTitle = "Clean Code",
            coverRes = R.drawable.ic_book_placeholder,
            startDate = today.minusDays(1),
            dueDate = today.plusDays(7),
            status = "SOLICITADO"
        )

        // APROVADO
        memory += Loan(
            id = UUID.randomUUID().toString(),
            userEmail = email,
            bookId = "refactoring",
            bookTitle = "Refactoring",
            coverRes = R.drawable.ic_book_placeholder,
            startDate = today.minusDays(3),
            dueDate = today.plusDays(10),
            status = "APROVADO",
            pickupDate = today.minusDays(3)
        )

        // Atrasado
        memory += Loan(
            id = UUID.randomUUID().toString(),
            userEmail = email,
            bookId = "algorithms",
            bookTitle = "Algoritmos – Estruturas de Dados e Análise",
            coverRes = R.drawable.ic_book_placeholder,
            startDate = today.minusDays(25),
            dueDate = today.minusDays(2),
            status = "APROVADO",
            pickupDate = today.minusDays(25)
        )

        // Devolvido (histórico)
        memory += Loan(
            id = UUID.randomUUID().toString(),
            userEmail = email,
            bookId = "design_patterns",
            bookTitle = "Padrões de Projeto",
            coverRes = R.drawable.ic_book_placeholder,
            startDate = today.minusDays(60),
            dueDate = today.minusDays(45),
            returnedDate = today.minusDays(44),
            status = "DEVOLVIDO",
            pickupDate = today.minusDays(60)
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun aprovar(loanId: String, pickupDate: LocalDate = LocalDate.now(), dias: Long = 7): Boolean {
        val l = memory.find { it.id == loanId } ?: return false
        if (l.status != "SOLICITADO") return false
        l.status = "APROVADO"
        l.pickupDate = pickupDate
        l.dueDate = pickupDate.plusDays(dias)
        return true
    }

    fun recusar(loanId: String): Boolean {
        val l = memory.find { it.id == loanId } ?: return false
        if (l.status != "SOLICITADO") return false
        l.status = "RECUSADO"
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarDevolucao(loanId: String): Boolean {
        val l = memory.find { it.id == loanId } ?: return false
        if (l.isReturned) return false
        l.returnedDate = LocalDate.now()
        l.status = "DEVOLVIDO"
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun renovar(loanId: String, dias: Long = 7): Boolean {
        val l = memory.find { it.id == loanId } ?: return false
        if (l.isReturned) return false
        if (l.renewCount >= 2) return false
        l.dueDate = l.dueDate.plusDays(dias)
        l.renewCount += 1
        l.status = "RENOVADO"
        return true
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun renew(loanId: String, days: Long = 7): Boolean = renovar(loanId, days)

    @RequiresApi(Build.VERSION_CODES.O)
    fun returnBook(loanId: String): Boolean = registrarDevolucao(loanId)
}
