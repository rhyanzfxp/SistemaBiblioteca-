package com.example.myapplication.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID


class AdminBookStore(context: Context) {
    private val prefs = context.getSharedPreferences("admin_books", Context.MODE_PRIVATE)

    fun list(): List<Book> = load()

    fun add(
        title: String,
        author: String,
        type: String,
        year: Int,
        language: String,
        theme: String,
        edition: String?,
        synopsis: String?,
        coverRes: Int,
        availableCopies: Int,
        sector: String?,
        shelfCode: String?
    ): Book {
        val b = Book(
            id = UUID.randomUUID().toString(),
            title = title, author = author, type = type, year = year,
            language = language, theme = theme, edition = edition,
            synopsis = synopsis, coverRes = coverRes,
            availableCopies = availableCopies, sector = sector, shelfCode = shelfCode
        )
        save(list() + b)
        return b
    }

    fun update(book: Book) {
        val items = list().toMutableList()
        val idx = items.indexOfFirst { it.id == book.id }
        if (idx >= 0) {
            items[idx] = book
            save(items)
        }
    }

    fun delete(id: String) {
        save(list().filterNot { it.id == id })
    }


    private fun load(): List<Book> {
        val raw = prefs.getString("data", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = ArrayList<Book>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out += Book(
                id = o.getString("id"),
                title = o.getString("title"),
                author = o.getString("author"),
                type = o.getString("type"),
                year = o.getInt("year"),
                language = o.getString("language"),
                theme = o.getString("theme"),
                edition = o.optString("edition").ifBlank { null },
                synopsis = o.optString("synopsis").ifBlank { null },
                coverRes = o.getInt("coverRes"),
                availableCopies = o.optInt("availableCopies", 0),
                sector = o.optString("sector").ifBlank { null },
                shelfCode = o.optString("shelfCode").ifBlank { null }
            )
        }
        return out
    }

    private fun save(items: List<Book>) {
        val arr = JSONArray()
        for (b in items) {
            arr.put(JSONObject().apply {
                put("id", b.id)
                put("title", b.title)
                put("author", b.author)
                put("type", b.type)
                put("year", b.year)
                put("language", b.language)
                put("theme", b.theme)
                put("edition", b.edition ?: "")
                put("synopsis", b.synopsis ?: "")
                put("coverRes", b.coverRes)
                put("availableCopies", b.availableCopies)
                put("sector", b.sector ?: "")
                put("shelfCode", b.shelfCode ?: "")
            })
        }
        prefs.edit().putString("data", arr.toString()).apply()
    }
}
