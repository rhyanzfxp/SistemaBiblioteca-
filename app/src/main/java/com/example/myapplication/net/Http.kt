package com.example.myapplication.net

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Http {
    fun retrofit(context: Context): Retrofit {
        val base = ApiConfig.baseUrl(context)
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(ErrorInterceptor())
            .addInterceptor(logging)
            .build()


        return Retrofit.Builder()
            .baseUrl(if (base.isEmpty()) "http://localhost/" else "$base/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    fun api(context: Context): ApiService {
        return retrofit(context).create(ApiService::class.java)
    }
}
