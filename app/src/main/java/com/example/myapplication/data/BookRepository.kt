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
        val mapped = items.map { dto ->
            val finalTheme = (theme ?: dto.tags?.firstOrNull()).orEmpty()
            val sector = dto.sector ?: dto.shelfCode?.firstOrNull()?.toString()
            val shelf = dto.shelfCode

            Book(
                id = dto._id,
                title = dto.title,
                author = dto.author,
                type = type ?: "FISICO",
                year = year ?: 2024,
                language = language ?: "Português",
                theme = finalTheme,
                edition = null,
                synopsis = dto.description,
                coverUrl = dto.coverUrl,
                availableCopies = dto.copiesAvailable ?: 0,
                sector = sector,
                shelfCode = shelf
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
        val dto = try {
            api?.getBook(id)
        } catch (e: Exception) {
            null
        } ?: return@runBlocking null

        val sector = dto.sector ?: dto.shelfCode?.firstOrNull()?.toString()
        val shelf = dto.shelfCode

        return@runBlocking Book(
            id = dto._id,
            title = dto.title,
            author = dto.author,
            type = "FISICO",
            year = 2024,
            language = "Português",
            theme = dto.tags?.firstOrNull() ?: "",
            edition = null,
            synopsis = dto.description,
            coverUrl = dto.coverUrl,
            availableCopies = dto.copiesAvailable ?: 0,
            sector = sector,
            shelfCode = shelf
        )
    }



    fun all(): List<Book> = runBlocking {
        val items = api?.listBooks(null).orEmpty()
        items.map { dto ->
            val sector = dto.sector ?: dto.shelfCode?.firstOrNull()?.toString()
            val shelf = dto.shelfCode

            Book(
                id = dto._id,
                title = dto.title,
                author = dto.author,
                type = "FISICO",
                year = 2024,
                language = "Português",
                theme = dto.tags?.firstOrNull() ?: "",
                edition = null,
                synopsis = dto.description,
                coverUrl = dto.coverUrl,
                availableCopies = dto.copiesAvailable ?: 0,
                sector = sector,
                shelfCode = shelf
            )
        }
    }

    fun allAuthors(): List<String> = runBlocking {
        api?.listBooks(null)
            ?.map { it.author }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?.sorted()
            ?: emptyList()
    }

    fun allThemes(): List<String> = runBlocking {
        api?.listBooks(null)
            ?.flatMap { it.tags ?: emptyList() }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?.sorted()
            ?: emptyList()
    }

    fun allTypes(): List<String> = listOf("FISICO", "EBOOK")
    fun allYears(): List<Int> = (1990..2025).toList()
    fun allLanguages(): List<String> = listOf("Português", "Inglês", "Espanhol")
}