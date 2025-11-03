package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.R
import com.example.myapplication.net.ApiConfig
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.CreateBookRequest
import com.example.myapplication.net.Http
import com.example.myapplication.net.UpdateBookRequest
import kotlinx.coroutines.runBlocking

class AdminBookStore(context: Context) {
    private val ctx = context
    private val api: ApiService? by lazy {
        val base = ApiConfig.baseUrl(ctx)
        if (base.isEmpty()) null else Http.retrofit(ctx).create(ApiService::class.java)
    }

    fun list(): List<Book> = runBlocking {
        api?.let { api ->
            val items = api.listBooks(null)
            return@runBlocking items.map {
                Book(
                    id = it._id,
                    title = it.title,
                    author = it.author,
                    type = "FISICO",
                    year = 2024,
                    language = "Português",
                    theme = it.tags?.firstOrNull() ?: "",
                    edition = null,
                    synopsis = it.description,
                    coverRes = R.drawable.ic_book_placeholder,
                    availableCopies = it.copiesAvailable ?: 0,
                    sector = null,
                    shelfCode = it.isbn
                )
            }
        }
        emptyList()
    }

    fun add(
        title: String,
        author: String,
        type: String? = null,
        year: Int? = null,
        language: String? = null,
        theme: String? = null,
        edition: String? = null,
        synopsis: String? = null,
        coverRes: Int? = null,
        availableCopies: Int = 1,
        sector: String? = null,
        shelfCode: String? = null,
        copies: Int = availableCopies
    ) {
        runBlocking {
            api?.createBook(CreateBookRequest(
                title = title, author = author, isbn = shelfCode,
                copiesTotal = copies, copiesAvailable = copies,
                description = synopsis, tags = theme?.let { listOf(it) }
            ))
        }
    }

    fun update(book: Book) {
        runBlocking {
            api?.updateBook(book.id, UpdateBookRequest(
                title = book.title, author = book.author, isbn = book.shelfCode,
                copiesTotal = book.availableCopies, copiesAvailable = book.availableCopies,
                description = book.synopsis, tags = if (book.theme.isBlank()) null else listOf(book.theme)
            ))
        }
    }

    fun delete(id: String) {
        runBlocking { api?.deleteBook(id) }
    }
}
