package com.example.myapplication.net

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val req = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else chain.request()
        return chain.proceed(req)
    }
}
