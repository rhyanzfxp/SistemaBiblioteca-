package com.example.myapplication.net

import retrofit2.http.*

data class ForgotRequest(val email: String)
data class ResetRequest(val token: String, val password: String)

interface ApiService {


    @POST("auth/login")
    suspend fun login(@Body body: AuthRequest): AuthResponse

    @POST("auth/admin/login")
    suspend fun adminLogin(@Body body: AuthRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse



    @GET("books")
    suspend fun listBooks(@Query("q") q: String? = null): List<BookDto>

    @GET("books/{id}")
    suspend fun getBook(@Path("id") id: String): BookDto

    @POST("books")
    suspend fun createBook(@Body body: CreateBookRequest): BookDto

    @PATCH("books/{id}")
    suspend fun updateBook(@Path("id") id: String, @Body body: UpdateBookRequest): BookDto

    @DELETE("books/{id}")
    suspend fun deleteBook(@Path("id") id: String)



    @GET("users")
    suspend fun listUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 200,
        @Query("q") q: String? = null
    ): UsersPage

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserItem

    @PATCH("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body body: UpdateUserRequest): UserItem

    @PATCH("users/{id}/status")
    suspend fun updateUserStatus(@Path("id") id: String, @Body body: UpdateStatusRequest): UserItem

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Map<String, Any>



    @POST("auth/forgot")
    suspend fun forgot(@Body body: ForgotRequest): Map<String, Any>

    @POST("auth/reset")
    suspend fun reset(@Body body: ResetRequest): Map<String, Any>

}
