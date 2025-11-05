package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.Http
import com.example.myapplication.net.NotificationDto

class NotificationStore(ctx: Context) {
    private val api = Http.retrofit(ctx).create(ApiService::class.java)

    suspend fun list(onlyUnread: Boolean = false): List<NotificationDto> =
        api.notifications(onlyUnread)

    suspend fun markRead(id: String) {
        api.markRead(id)
    }
}
