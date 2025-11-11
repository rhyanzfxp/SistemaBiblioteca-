package com.example.myapplication.data

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class ChatRequest(val question: String)
data class ChatResponse(val reply: String?)

interface ChatApi {
    @POST("/api/chat")
    suspend fun sendMessage(@Body body: ChatRequest): ChatResponse

    companion object {
        fun create(): ChatApi {
            return Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080") // para o emulador Android acessar localhost
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ChatApi::class.java)
        }
    }
}
