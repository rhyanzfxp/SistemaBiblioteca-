package com.example.myapplication.data

import android.content.Context

class FavoritesStore(context: Context) {
    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun isFavorite(userEmail: String, bookId: String): Boolean {
        val key = key(userEmail)
        val set = prefs.getStringSet(key, emptySet()) ?: emptySet()
        return set.contains(bookId)
    }

    fun toggle(userEmail: String, bookId: String): Boolean {
        val key = key(userEmail)
        val set = prefs.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()
        val added: Boolean
        if (set.contains(bookId)) {
            set.remove(bookId)
            added = false
        } else {
            set.add(bookId)
            added = true
        }
        prefs.edit().putStringSet(key, set).apply()
        return added
    }

    fun list(userEmail: String): Set<String> {
        return prefs.getStringSet(key(userEmail), emptySet()) ?: emptySet()
    }

    private fun key(email: String) = "fav_${email.lowercase()}"
}