package com.example.myapplication.data

import java.time.LocalDate

data class Loan(
    val id: String,
    val userEmail: String,
    val bookId: String,
    val bookTitle: String,
    val coverRes: Int,
    val startDate: LocalDate,
    var dueDate: LocalDate,
    var returnedDate: LocalDate? = null,
    var renewCount: Int = 0
) {
    val isReturned: Boolean get() = returnedDate != null
    val isOverdue: Boolean get() = !isReturned && LocalDate.now().isAfter(dueDate)
}
