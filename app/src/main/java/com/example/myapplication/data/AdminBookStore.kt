package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.R
import com.example.myapplication.net.ApiConfig
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.CreateBookRequest
import com.example.myapplication.net.Http
import com.example.myapplication.net.UpdateBookRequest
import com.example.myapplication.core.toFile
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AdminBookStore(context: Context ) {

    private val ctx = context

    // PREFS onde salvamos o caminho da capa
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
                    coverUrl = it.coverUrl,
                    availableCopies = it.copiesAvailable ?: 0,
                    sector = it.sector,
                    shelfCode = it.shelfCode
                )
            }
        }
        emptyList()
    }

    fun add(
        title: String,
        author: String,
        type: String,
        year: Int,
        language: String? = null,
        theme: String? = null,
        edition: String? = null,
        synopsis: String? = null,
        availableCopies: Int = 1,
        coverUri: android.net.Uri? = null,
        sector: String? = null,
        shelfCode: String? = null,
        copies: Int = availableCopies
    ) {
        runBlocking {
            val mediaType = "text/plain".toMediaTypeOrNull()
            val tags = theme?.let { listOf(it).joinToString(",") }

            api?.createBook(
                title = title.toRequestBody(mediaType),
                author = author.toRequestBody(mediaType),
                isbn = null,
                copiesTotal = copies.toString().toRequestBody(mediaType),
                copiesAvailable = copies.toString().toRequestBody(mediaType),
                tags = tags?.toRequestBody(mediaType),
                sector = sector?.toRequestBody(mediaType),
                shelfCode = shelfCode?.toRequestBody(mediaType),
                description = synopsis?.toRequestBody(mediaType),
                cover = coverUri?.toFile(ctx)
            )
        }
    }

    fun update(book: Book, coverUri: android.net.Uri? = null) {
        runBlocking {
            val mediaType = "text/plain".toMediaTypeOrNull()
            val tags = if (book.theme.isBlank()) null else listOf(book.theme).joinToString(",")

            api?.updateBook(
                book.id,
                title = book.title.toRequestBody(mediaType),
                author = book.author.toRequestBody(mediaType),
                isbn = null,
                copiesTotal = book.availableCopies.toString().toRequestBody(mediaType),
                copiesAvailable = book.availableCopies.toString().toRequestBody(mediaType),
                tags = tags?.toRequestBody(mediaType),
                sector = book.sector?.toRequestBody(mediaType),
                shelfCode = book.shelfCode?.toRequestBody(mediaType),
                description = book.synopsis?.toRequestBody(mediaType),
                coverUrl = if (coverUri == null) book.coverUrl?.toRequestBody(mediaType) else null,
                cover = coverUri?.toFile(ctx)
            )
        }
    }

    fun delete(id: String) {
        runBlocking { api?.deleteBook(id) }
    }
}
