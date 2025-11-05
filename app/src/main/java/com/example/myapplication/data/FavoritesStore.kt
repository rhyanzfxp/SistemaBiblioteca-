package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.Http
import com.example.myapplication.net.BookDto

class FavoritesStore(ctx: Context) {
    private val api = Http.retrofit(ctx).create(ApiService::class.java)

    suspend fun list(): List<BookDto> = api.favorites()

    suspend fun add(bookId: String) { api.addFavorite(bookId) }

    suspend fun remove(bookId: String) { api.removeFavorite(bookId) }
}
