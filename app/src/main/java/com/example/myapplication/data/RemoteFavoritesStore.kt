package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.BookDto
import com.example.myapplication.net.Http


class RemoteFavoritesStore(ctx: Context) {
    private val api: ApiService = Http.retrofit(ctx).create(ApiService::class.java)


    private var cachedIds: MutableSet<String>? = null

    suspend fun list(): List<BookDto> {
        val items = api.favorites()
        cachedIds = items.mapNotNull { it._id }.toMutableSet()
        return items
    }

    suspend fun isFavorite(bookId: String): Boolean {
        val set = cachedIds ?: list().map { it._id }.toMutableSet().also { cachedIds = it }
        return set.contains(bookId)
    }


    suspend fun toggle(bookId: String): Boolean {
        val set = cachedIds ?: mutableSetOf()
        val becameFav: Boolean
        if (set.contains(bookId)) {
            api.removeFavorite(bookId)
            set.remove(bookId)
            becameFav = false
        } else {
            api.addFavorite(bookId)
            set.add(bookId)
            becameFav = true
        }
        cachedIds = set
        return becameFav
    }


    fun evictFromCache(bookId: String) {
        cachedIds?.remove(bookId)
    }
}
