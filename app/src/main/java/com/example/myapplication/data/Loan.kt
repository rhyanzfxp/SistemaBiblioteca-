package com.example.myapplication.data

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

data class Loan(
    val id: String,
    val userEmail: String,
    val bookId: String,
    val bookTitle: String,
    val coverRes: Int,

    // Datas do fluxo
    val startDate: LocalDate,        // data da solicitação/criação
    var dueDate: LocalDate,          // data de devolução prevista
    var returnedDate: LocalDate? = null,

    // NOVO: RF20/RF20.1
    var status: String = "SOLICITADO",          // SOLICITADO | APROVADO | RECUSADO | DEVOLVIDO | RENOVADO
    var pickupDate: LocalDate? = null,          // data de retirada (ao aprovar)
    var renewCount: Int = 0
) {
    val isReturned: Boolean get() = returnedDate != null
    val isOverdue: Boolean @RequiresApi(Build.VERSION_CODES.O)
    get() = !isReturned && LocalDate.now().isAfter(dueDate)
}
