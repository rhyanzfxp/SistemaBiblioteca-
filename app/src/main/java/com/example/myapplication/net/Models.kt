package com.example.myapplication.net

// ---------- AUTH ----------
data class AuthRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)

data class UserLite(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val active: Boolean
)

data class AuthResponse(
    val token: String,
    val user: UserLite
)

data class RegisterResponse(val ok: Boolean? = null, val id: String? = null)


// ---------- BOOKS ----------
data class BookDto(
    val _id: String,
    val title: String,
    val author: String,
    val isbn: String? = null,
    val copiesTotal: Int? = null,
    val copiesAvailable: Int? = null,
    val tags: List<String>? = null,
    val coverUrl: String? = null,
    val description: String? = null
)

data class CreateBookRequest(
    val title: String,
    val author: String,
    val isbn: String? = null,
    val copiesTotal: Int? = 1,
    val copiesAvailable: Int? = null,
    val tags: List<String>? = null,
    val coverUrl: String? = null,
    val description: String? = null
)

data class UpdateBookRequest(
    val title: String? = null,
    val author: String? = null,
    val isbn: String? = null,
    val copiesTotal: Int? = null,
    val copiesAvailable: Int? = null,
    val tags: List<String>? = null,
    val coverUrl: String? = null,
    val description: String? = null
)


// ---------- USERS (ADMIN) ----------
data class UserItem(
    val _id: String,
    val name: String,
    val email: String,
    val role: String,
    val active: Boolean = true
)

data class UsersPage(
    val items: List<UserItem>,
    val total: Int,
    val page: Int,
    val pages: Int
)

data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,    // "user" | "admin"
    val password: String? = null // redefinição de senha
)

data class UpdateStatusRequest(val active: Boolean)

// (opcional, só se algum lugar usar)
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "user"
)
