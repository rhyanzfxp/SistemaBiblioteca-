
package com.example.myapplication.data

import android.content.Context
import android.util.Base64
import java.security.MessageDigest

data class User(val name: String, val email: String, val passwordHash: String)

class UserStore(context: Context) {
    private val prefs = context.getSharedPreferences("users", Context.MODE_PRIVATE)

    private fun key(email: String) = "user_${email.lowercase()}"

    fun createUser(name: String, email: String, password: String): Boolean {
        val k = key(email)
        if (prefs.contains(k)) return false
        val hash = hash(password)
        val user = User(name, email, hash)
        prefs.edit().putString(k, serialize(user)).apply()
        return true
    }

    fun findUser(email: String): User? {
        val s = prefs.getString(key(email), null) ?: return null
        return deserialize(s)
    }

    fun validateLogin(email: String, password: String): Boolean {
        val u = findUser(email) ?: return false
        return u.passwordHash == hash(password)
    }

    fun resetPassword(email: String, newPassword: String): Boolean {
        val u = findUser(email) ?: return false
        val updated = u.copy(passwordHash = hash(newPassword))
        prefs.edit().putString(key(email), serialize(updated)).apply()
        return true
    }

    private fun hash(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    private fun serialize(u: User): String = listOf(u.name, u.email, u.passwordHash).joinToString("|")
    private fun deserialize(s: String): User {
        val parts = s.split("|")
        return User(parts.getOrElse(0){""}, parts.getOrElse(1){""}, parts.getOrElse(2){""})
    }
}
