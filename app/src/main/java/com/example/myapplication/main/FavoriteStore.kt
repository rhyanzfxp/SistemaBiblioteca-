package com.example.myapplication.main

import android.content.Context

object FavoriteStore {
    private const val PREFS = "fav_prefs"
    private const val KEY = "fav_ids"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getIds(ctx: Context): Set<Int> =
        sp(ctx).getStringSet(KEY, emptySet())!!.mapNotNull { it.toIntOrNull() }.toSet()

    fun isFavorite(ctx: Context, id: Int): Boolean = id in getIds(ctx)

    fun toggle(ctx: Context, id: Int) {
        val set = getIds(ctx).toMutableSet()
        if (id in set) set.remove(id) else set.add(id)
        sp(ctx).edit().putStringSet(KEY, set.map { it.toString() }.toSet()).apply()
    }
}
