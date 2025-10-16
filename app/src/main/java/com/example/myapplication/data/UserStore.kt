package com.example.myapplication.data

import android.content.Context
import android.util.Base64
import java.security.MessageDigest

data class User(
    val name: String,
    val email: String,
    val passwordHash: String
)

class UserStore(context: Context) {

    private val prefs = context.getSharedPreferences("users", Context.MODE_PRIVATE)

    fun register(name: String, email: String, password: String): Boolean {
        if (prefs.contains(key(email))) return false
        val user = User(name, email, hash(password))
        prefs.edit().putString(key(email), serialize(user)).apply()
        prefs.edit().putString("current", email).apply()
        return true
    }

    fun login(email: String, password: String): Boolean {
        val raw = prefs.getString(key(email), null) ?: return false
        val u = deserialize(raw)
        val ok = u.passwordHash == hash(password)
        if (ok) prefs.edit().putString("current", email).apply()
        return ok
    }

    fun logout() {
        prefs.edit().remove("current").apply()
    }

    fun currentUserEmail(): String? = prefs.getString("current", null)

    fun currentUser(): User? {
        val email = currentUserEmail() ?: return null
        val raw = prefs.getString(key(email), null) ?: return null
        return deserialize(raw)
    }

    fun updateName(email: String, newName: String) {
        val k = key(email)
        val raw = prefs.getString(k, null) ?: return
        val u = deserialize(raw)
        val updated = User(newName, u.email, u.passwordHash)
        prefs.edit().putString(k, serialize(updated)).apply()
    }

    private fun key(email: String) = "user_${email.lowercase()}"

    private fun hash(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    private fun serialize(u: User): String =
        listOf(u.name, u.email, u.passwordHash).joinToString("|")

    private fun deserialize(s: String): User {
        val parts = s.split("|")
        return User(
            parts.getOrElse(0) { "" },
            parts.getOrElse(1) { "" },
            parts.getOrElse(2) { "" }
        )
    }
}
