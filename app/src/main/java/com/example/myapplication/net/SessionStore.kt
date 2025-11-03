
package com.example.myapplication.net

import android.content.Context

class SessionStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)


    fun save(token: String, id: String, name: String, email: String, role: String) {
        prefs.edit()
            .putString("token", token)
            .putString("user_id", id)
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_role", role.trim().lowercase())
            .apply()
    }

    fun clear() { prefs.edit().clear().apply() }

    fun token(): String? = prefs.getString("token", null)
    fun roleRaw(): String? = prefs.getString("user_role", null)
    fun role(): String = (roleRaw() ?: "").trim().lowercase()
    fun name(): String? = prefs.getString("user_name", null)
    fun email(): String? = prefs.getString("user_email", null)
    fun id(): String? = prefs.getString("user_id", null)

    fun isAdmin(): Boolean = role() == "admin"
}
