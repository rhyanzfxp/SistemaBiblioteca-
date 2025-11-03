package com.example.myapplication.data

import android.content.Context
import android.util.Base64
import java.security.MessageDigest

data class User(
    val name: String,
    val email: String,
    val passwordHash: String,
    val perfil: String = "aluno",
    val active: Boolean = true
)

class UserStore(context: Context) {

    private val ctx = context
    private val prefs = context.getSharedPreferences("users", Context.MODE_PRIVATE)

    fun register(name: String, email: String, password: String, perfil: String = "aluno", active: Boolean = true): Boolean {
        if (prefs.contains(key(email))) return false
        val user = User(name, email, hash(password), perfil, active)
        prefs.edit().putString(key(email), serialize(user)).apply()
        prefs.edit().putString("current", email).apply()
        return true
    }

    fun login(email: String, password: String): Boolean {
        val raw = prefs.getString(key(email), null) ?: return false
        val u = deserialize(raw)
        val ok = u.passwordHash == hash(password)
        if (!ok) return false
        if (!u.active) return false
        prefs.edit().putString("current", email).apply()
        return true
    }

    fun logout() { prefs.edit().remove("current").apply() }

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
        save(u.copy(name = newName))
    }

    fun setActive(email: String, active: Boolean) {
        val u = get(email) ?: return
        save(u.copy(active = active))
    }

    fun setPerfil(email: String, perfil: String) {
        val u = get(email) ?: return
        save(u.copy(perfil = perfil))
    }

    fun delete(email: String) {
        prefs.edit().remove(key(email)).apply()
        if (currentUserEmail()?.equals(email, true) == true) logout()
    }

    fun allUsers(): List<User> {
        return prefs.all
            .filterKeys { it.startsWith("user_") }
            .values
            .mapNotNull { it as? String }
            .map { deserialize(it) }
            .sortedBy { it.name.lowercase() }
    }

    // ===== Helpers =====
    private fun get(email: String): User? {
        val raw = prefs.getString(key(email), null) ?: return null
        return deserialize(raw)
    }

    private fun save(u: User) {
        prefs.edit().putString(key(u.email), serialize(u)).apply()
    }

    private fun key(email: String) = "user_${email.lowercase()}"

    private fun hash(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    private fun serialize(u: User): String =
        listOf(u.name, u.email, u.passwordHash, u.perfil, u.active.toString()).joinToString("|")

    private fun deserialize(s: String): User {
        val p = s.split("|")
        return User(
            name = p.getOrElse(0) { "" },
            email = p.getOrElse(1) { "" },
            passwordHash = p.getOrElse(2) { "" },
            perfil = p.getOrElse(3) { "aluno" },
            active = p.getOrElse(4) { "true" }.toBooleanStrictOrNull() ?: true
        )
    }

    fun ensureAdmin(
        name: String = "Administrador",
        email: String = "admin@local",
        password: String = "admin123"
    ) {
        val k = key(email)
        val raw = prefs.getString(k, null)
        if (raw == null) {
            register(name, email, password, perfil = "admin", active = true)
            logout()
        } else {
            val u = deserialize(raw)
            prefs.edit().putString(k, serialize(u.copy(perfil = "admin", active = true))).apply()
        }
    }

    // ==== REMOTO (API) ====  <<--- agora fora do ensureAdmin()
    suspend fun remoteLogin(email: String, password: String): Boolean {
        val base = com.example.myapplication.net.ApiConfig.baseUrl(ctx)
        if (base.isEmpty()) return false
        val api = com.example.myapplication.net.Http
            .retrofit(ctx)
            .create(com.example.myapplication.net.ApiService::class.java)

        val res = api.login(com.example.myapplication.net.AuthRequest(email, password))

        // token vai para "session", onde o interceptor lê
        ctx.getSharedPreferences("session", Context.MODE_PRIVATE)
            .edit().putString("token", res.token).apply()

        val perfil = if (res.user.role == "admin") "admin" else "aluno"
        val user = User(res.user.name, res.user.email, "", perfil, true)
        prefs.edit().putString(key(email), serialize(user)).apply()
        prefs.edit().putString("current", email).apply()
        return true
    }

    suspend fun remoteAdminLogin(email: String, password: String): Boolean {
        val base = com.example.myapplication.net.ApiConfig.baseUrl(ctx)
        if (base.isEmpty()) return false
        val api = com.example.myapplication.net.Http
            .retrofit(ctx)
            .create(com.example.myapplication.net.ApiService::class.java)

        val res = api.adminLogin(com.example.myapplication.net.AuthRequest(email, password))

        ctx.getSharedPreferences("session", Context.MODE_PRIVATE)
            .edit().putString("token", res.token).apply()

        val user = User(res.user.name, res.user.email, "", "admin", true)
        prefs.edit().putString(key(email), serialize(user)).apply()
        prefs.edit().putString("current", email).apply()
        return true
    }

    suspend fun remoteRegister(name: String, email: String, password: String): Boolean {
        val base = com.example.myapplication.net.ApiConfig.baseUrl(ctx)
        if (base.isEmpty()) return false
        val api = com.example.myapplication.net.Http
            .retrofit(ctx)
            .create(com.example.myapplication.net.ApiService::class.java)

        api.register(com.example.myapplication.net.RegisterRequest(name, email, password))
        return remoteLogin(email, password)
    }
}
