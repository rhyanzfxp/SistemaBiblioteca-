package com.example.myapplication.net

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)


        if (response.code == 409) {
            val body = response.peekBody(Long.MAX_VALUE).string()
            if (body.contains("email informado já está cadastrado", ignoreCase = true)) {
                throw IOException("O email informado já está cadastrado")
            }
        }


        if (response.code == 403) {
            val body = response.peekBody(Long.MAX_VALUE).string()
            if (body.contains("inativa", ignoreCase = true)) {
                throw IOException("Sua conta está inativa. Entre em contato com a administração.")
            }
        }

        return response
    }
}
