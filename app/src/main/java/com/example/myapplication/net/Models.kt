package com.example.myapplication.net


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


data class BookDto(
    val _id: String,
    val title: String,
    val author: String,
    val isbn: String? = null,
    val copiesTotal: Int? = null,
    val copiesAvailable: Int? = null,
    val tags: List<String>? = null,
    val coverUrl: String? = null,
    val sector: String? = null,
    val shelfCode: String? = null,
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
    val sector: String? = null,
    val shelfCode: String? = null,
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
    val sector: String? = null,
    val shelfCode: String? = null,
    val description: String? = null
)


data class BookMin(
    val _id: String? = null,
    val title: String? = null,
    val author: String? = null,
    val coverUrl: String? = null
)


data class UserItem(
    val _id: String,
    val name: String,
    val email: String,
    val role: String,
    val active: Boolean = true,
    val photoUrl: String? = null
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
    val photoUrl: String? = null,
    val role: String? = null,
    val password: String? = null
)

data class UpdateStatusRequest(val active: Boolean)

data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "user"
)

data class UserMin(
    val _id: String? = null,
    val name: String? = null,
    val email: String? = null
)

data class LoanDto(
    val _id: String,
    val userId: UserMin? = null,
    val bookId: BookMin? = null,
    val status: String,
    val reason: String? = null,
    val requestedAt: String? = null,
    val approvedAt: String? = null,
    val startDate: String? = null,
    val dueDate: String? = null,
    val returnedAt: String? = null,
    val renewCount: Int? = 0,


    val renewalRequested: Boolean? = null,
    val renewalAddDays: Int? = null,
    val renewalReason: String? = null,
    val renewalRequestedAt: String? = null,
    val renewalReviewedAt: String? = null,
    val renewalDeniedReason: String? = null
)


data class AreaDto(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

data class LocationsResponse(
    val ground: Map<String, AreaDto>,
    val upper: Map<String, AreaDto>
)

data class NotificationDto(
    val _id: String,
    val title: String,
    val body: String,
    val read: Boolean,
    val createdAt: String
)
