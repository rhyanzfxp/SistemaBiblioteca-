package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.Http
import com.example.myapplication.net.LoanDto
import com.example.myapplication.net.RequestLoanBody

class LoanRepository(ctx: Context) {
    private val api: ApiService = Http.retrofit(ctx).create(ApiService::class.java)


    suspend fun request(bookId: String) {
        api.requestLoan(RequestLoanBody(bookId))
    }


    suspend fun myLoans(activeOnly: Boolean = false): List<LoanDto> =
        api.myLoans(active = activeOnly)



    suspend fun adminList(): List<LoanDto> = api.adminListLoans()



    suspend fun approve(id: String, days: Int = 7) {
        api.adminApprove(id, mapOf("days" to days))
    }

    suspend fun deny(id: String, reason: String) {
        api.adminDeny(id, mapOf("reason" to reason))
    }

    suspend fun returnBook(id: String) {
        api.adminReturn(id)
    }

    suspend fun renew(id: String, days: Int) {
        api.adminRenew(id, mapOf("days" to days))
    }
}
