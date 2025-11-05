package com.example.myapplication.net

import retrofit2.http.*

// ===== Bodies =====
data class ForgotRequest(val email: String)
data class ResetRequest(val token: String, val password: String)
data class RequestLoanBody(val bookId: String)

// ===== API =====
interface ApiService {

    // AUTH
    @POST("auth/login") suspend fun login(@Body body: AuthRequest): AuthResponse
    @POST("auth/admin/login") suspend fun adminLogin(@Body body: AuthRequest): AuthResponse
    @POST("auth/register") suspend fun register(@Body body: RegisterRequest): RegisterResponse
    @POST("auth/forgot") suspend fun forgot(@Body body: ForgotRequest): Map<String, Any>
    @POST("auth/reset") suspend fun reset(@Body body: ResetRequest): Map<String, Any>

    // BOOKS
    @GET("books") suspend fun listBooks(@Query("q") q: String? = null): List<BookDto>
    @GET("books/{id}") suspend fun getBook(@Path("id") id: String): BookDto
    @POST("books") suspend fun createBook(@Body body: CreateBookRequest): BookDto
    @PATCH("books/{id}") suspend fun updateBook(@Path("id") id: String, @Body body: UpdateBookRequest): BookDto
    @DELETE("books/{id}") suspend fun deleteBook(@Path("id") id: String)

    // USERS
    @GET("users") suspend fun listUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 200,
        @Query("q") q: String? = null
    ): UsersPage
    @GET("users/{id}") suspend fun getUser(@Path("id") id: String): UserItem
    @PATCH("users/{id}") suspend fun updateUser(@Path("id") id: String, @Body body: UpdateUserRequest): UserItem
    @PATCH("users/{id}/status") suspend fun updateUserStatus(@Path("id") id: String, @Body body: UpdateStatusRequest): UserItem
    @DELETE("users/{id}") suspend fun deleteUser(@Path("id") id: String): Map<String, Any>

    @POST("loans")
    suspend fun requestLoan(@Body body: RequestLoanBody): Map<String, Any>

    @GET("loans/me") suspend fun myLoans(@Query("active") active: Boolean = false): List<LoanDto>


    @GET("admin/loans") suspend fun adminListLoans(): List<LoanDto>
    @PATCH("admin/loans/{id}/approve")
    suspend fun adminApprove(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Int>
    ): Map<String, Any>

    @PATCH("admin/loans/{id}/deny")
    suspend fun adminDeny(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards String>
    ): Map<String, Any>

    @PATCH("admin/loans/{id}/return")
    suspend fun adminReturn(@Path("id") id: String): Map<String, Any> //

    @PATCH("admin/loans/{id}/renew")
    suspend fun adminRenew(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Int>
    ): Map<String, Any> //

    // FAVORITES
    @GET("me/favorites") suspend fun favorites(): List<BookDto>
    @POST("me/favorites/{id}") suspend fun addFavorite(@Path("id") bookId: String): Map<String, Any>
    @DELETE("me/favorites/{id}") suspend fun removeFavorite(@Path("id") bookId: String): Map<String, Any>

    // NOTIFICATIONS (user)
    @GET("me/notifications")
    suspend fun notifications(@Query("onlyUnread") onlyUnread: Boolean = false): List<NotificationDto>
    @PATCH("me/notifications/{id}/read") suspend fun markRead(@Path("id") id: String): Map<String, Any>


    @POST("admin/notices")
    suspend fun adminCreateNotice(@Body body: Map<String, String>): Map<String, Any>
}
