package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.R
import com.example.myapplication.net.ApiConfig
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.Http
import kotlinx.coroutines.runBlocking

class BookRepository(private val context: Context) {
    private val api: ApiService? by lazy {
        val base = ApiConfig.baseUrl(context)
        if (base.isEmpty()) null else Http.retrofit(context).create(ApiService::class.java)
    }

    fun search(
        query: String = "",
        author: String? = null,
        theme: String? = null,
        type: String? = null,
        year: Int? = null,
        language: String? = null
    ): List<Book> = runBlocking {
        val items = api?.listBooks(query).orEmpty()
        val mapped = items.map {
            Book(
                id = it._id,
                title = it.title,
                author = it.author,
                type = type ?: "FISICO",
                year = year ?: 2024,
                language = language ?: "Português",
                theme = (theme ?: it.tags?.firstOrNull()).orEmpty(),
                edition = null,
                synopsis = it.description,
                coverRes = R.drawable.ic_book_placeholder,
                availableCopies = it.copiesAvailable ?: 0,
                sector = null,
                shelfCode = it.isbn
            )
        }
        mapped.filter { b ->
            (author == null || b.author.equals(author, true)) &&
            (theme == null || b.theme.equals(theme, true)) &&
            (type == null || b.type.equals(type, true)) &&
            (year == null || b.year == year) &&
            (language == null || b.language.equals(language, true)) &&
            (query.isBlank() || b.title.contains(query, true) || b.author.contains(query, true))
        }
    }

    fun byId(id: String): Book? = runBlocking {
        val items = api?.listBooks(null).orEmpty()
        val it = items.find { it._id == id } ?: return@runBlocking null
        return@runBlocking Book(
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

    fun allAuthors(): List<String> = runBlocking { api?.listBooks(null)?.map { it.author }?.filter { it.isNotBlank() }?.distinct()?.sorted() ?: emptyList() }
    fun allThemes(): List<String> = runBlocking { api?.listBooks(null)?.flatMap { it.tags ?: emptyList() }?.filter { it.isNotBlank() }?.distinct()?.sorted() ?: emptyList() }
    fun allTypes(): List<String> = listOf("FISICO", "EBOOK")
    fun allYears(): List<Int> = (1990..2025).toList()
    fun allLanguages(): List<String> = listOf("Português", "Inglês", "Espanhol")
}
