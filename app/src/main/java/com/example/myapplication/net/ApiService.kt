package com.example.myapplication.net

import retrofit2.http.*
import okhttp3.RequestBody
import okhttp3.MultipartBody

data class ForgotRequest(val email: String )
data class ResetRequest(val token: String, val password: String)
data class RequestLoanBody(val bookId: String)

data class AccessibilityPrefs(
    val fontSize: String = "normal",
    val contrast: Boolean = false,
    val voiceAssist: Boolean = false,
    val libras: Boolean = false
)


interface ApiService {

    // LOCATIONS
    @GET("locations") suspend fun getLocations(): LocationsResponse

    // AUTH
    @POST("auth/login") suspend fun login(@Body body: AuthRequest): AuthResponse
    @POST("auth/admin/login") suspend fun adminLogin(@Body body: AuthRequest): AuthResponse
    @POST("auth/register") suspend fun register(@Body body: RegisterRequest): RegisterResponse
    @POST("auth/forgot") suspend fun forgot(@Body body: ForgotRequest): Map<String, Any>
    @POST("auth/reset") suspend fun reset(@Body body: ResetRequest): Map<String, Any>

    // BOOKS
    @GET("books") suspend fun listBooks(@Query("q") q: String? = null): List<BookDto>
    @GET("books/{id}") suspend fun getBook(@Path("id") id: String): BookDto

    @Multipart
    @POST("books") suspend fun createBook(
        @Part("title") title: RequestBody,
        @Part("author" ) author: RequestBody,
        @Part("isbn" ) isbn: RequestBody?,
        @Part("copiesTotal" ) copiesTotal: RequestBody?,
        @Part("copiesAvailable" ) copiesAvailable: RequestBody?,
        @Part("tags" ) tags: RequestBody?,
        @Part("sector" ) sector: RequestBody?,
        @Part("shelfCode" ) shelfCode: RequestBody?,
        @Part("description" ) description: RequestBody?,
        @Part("year" ) year: RequestBody?, // Adicionado
        @Part("edition" ) edition: RequestBody?, // Adicionado
        @Part cover: MultipartBody.Part?
    ): BookDto

    @Multipart
    @PATCH("books/{id}") suspend fun updateBook(
        @Path("id") id: String,
        @Part("title") title: RequestBody?,
        @Part("author" ) author: RequestBody?,
        @Part("isbn" ) isbn: RequestBody?,
        @Part("copiesTotal" ) copiesTotal: RequestBody?,
        @Part("copiesAvailable" ) copiesAvailable: RequestBody?,
        @Part("tags" ) tags: RequestBody?,
        @Part("sector" ) sector: RequestBody?,
        @Part("shelfCode" ) shelfCode: RequestBody?,
        @Part("description" ) description: RequestBody?,
        @Part("year" ) year: RequestBody?, // Adicionado
        @Part("edition" ) edition: RequestBody?, // Adicionado
        @Part("coverUrl" ) coverUrl: RequestBody?,
        @Part cover: MultipartBody.Part?
    ): BookDto

    @DELETE("books/{id}") suspend fun deleteBook(@Path("id") id: String)



    @GET("books/recent")
    suspend fun recentBooks(@Query("limit") limit: Int? = null): List<BookDto>

    @GET("books/top-recommended")
    suspend fun topRecommended(@Query("limit") limit: Int? = null): List<BookDto>

    @GET("books/top-borrowed")
    suspend fun topBorrowed(@Query("limit") limit: Int? = null): List<BookDto>

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

    // LOANS
    @POST("loans")
    suspend fun requestLoan(@Body body: RequestLoanBody): Map<String, Any>

    @GET("loans/me")
    suspend fun myLoans(@Query("active") active: Boolean = false): List<LoanDto>

    // Admin - aprovação inicial de empréstimos
    @GET("admin/loans")
    suspend fun adminListLoans(): List<LoanDto>

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
    suspend fun adminReturn(@Path("id") id: String): Map<String, Any>

    @PATCH("admin/loans/{id}/renew")
    suspend fun adminRenew(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Int>
    ): Map<String, Any>


    @POST("loans/{id}/renew-request")
    suspend fun requestRenew(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any> = emptyMap() // ex: mapOf("addDays" to 7, "reason" to "atraso")
    ): Map<String, Any>


    @GET("admin/loans/renew-requests")
    suspend fun adminListRenewRequests(): List<LoanDto>

    // Aprovar solicitação de renovação
    @POST("admin/loans/{id}/renew-approve")
    suspend fun adminApproveRenew(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Int> = emptyMap() // opcional: "days"
    ): LoanDto

    // Negar solicitação de renovação
    @POST("admin/loans/{id}/renew-deny")
    suspend fun adminDenyRenew(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards String> = emptyMap() // opcional: "reason"
    ): Map<String, Any>

    // FAVORITES
    @GET("me/favorites") suspend fun favorites(): List<BookDto>
    @POST("me/favorites/{id}") suspend fun addFavorite(@Path("id") bookId: String): Map<String, Any>
    @DELETE("me/favorites/{id}") suspend fun removeFavorite(@Path("id") bookId: String): Map<String, Any>

    // NOTIFICATIONS
    @GET("me/notifications")
    suspend fun notifications(@Query("onlyUnread") onlyUnread: Boolean = false): List<NotificationDto>
    @PATCH("me/notifications/{id}/read") suspend fun markRead(@Path("id") id: String): Map<String, Any>

    // ADMIN NOTICES
    @POST("admin/notices")
    suspend fun adminCreateNotice(@Body body: Map<String, String>): Map<String, Any>



    // PROFILE
    @GET("users/me") suspend fun getMyProfile(): UserItem
    @PATCH("users/me") suspend fun updateMyProfile(@Body body: UpdateUserRequest): UserItem
    @GET("users/me/accessibility") suspend fun getAccessibility(): AccessibilityPrefs
    @PATCH("users/me/accessibility") suspend fun updateAccessibility(@Body body: AccessibilityPrefs): AccessibilityPrefs

    // PHOTO
    @Multipart
    @POST("users/me/photo")
    suspend fun uploadMyPhoto(@Part photo: MultipartBody.Part): UserItem

    @DELETE("users/me/photo")
    suspend fun deleteMyPhoto(): UserItem
}
