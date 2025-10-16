package com.example.myapplication.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = Instant.now().toEpochMilli(),
    var read: Boolean = false
)

class NotificationStore(context: Context) {
    private val prefs = context.getSharedPreferences("notifications", Context.MODE_PRIVATE)

    fun seedIfEmpty() {
        if (prefs.getString("seeded","0") == "1") return
        val sample = listOf(
            AppNotification("n1","Bem-vindo(a)!","Sua conta foi criada com sucesso.", read = true),
            AppNotification("n2","Empréstimo disponível","O livro Clean Code está disponível para retirada."),
            AppNotification("n3","Lembrete","Devolução em 2 dias: Estruturas de Dados em Kotlin.")
        )
        saveList(sample)
        prefs.edit().putString("seeded","1").apply()
    }

    fun all(): List<AppNotification> = loadList()
    fun unread(): List<AppNotification> = loadList().filter { !it.read }

    fun markRead(id: String) {
        val list = loadList().toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            list[idx] = list[idx].copy(read = true)
            saveList(list)
        }
    }

    private fun saveList(list: List<AppNotification>) {
        val arr = JSONArray()
        for (n in list) {
            val o = JSONObject()
            o.put("id", n.id)
            o.put("title", n.title)
            o.put("message", n.message)
            o.put("timestamp", n.timestamp)
            o.put("read", n.read)
            arr.put(o)
        }
        prefs.edit().putString("data", arr.toString()).apply()
    }

    private fun loadList(): List<AppNotification> {
        val json = prefs.getString("data", "[]") ?: "[]"
        val arr = JSONArray(json)
        val out = mutableListOf<AppNotification>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(AppNotification(
                id = o.getString("id"),
                title = o.getString("title"),
                message = o.getString("message"),
                timestamp = o.getLong("timestamp"),
                read = o.getBoolean("read")
            ))
        }
        return out.sortedByDescending { it.timestamp }
    }
}