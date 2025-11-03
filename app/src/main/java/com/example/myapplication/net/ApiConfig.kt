package com.example.myapplication.net

import android.content.Context
import com.example.myapplication.R

object ApiConfig {
    fun baseUrl(context: Context): String {
        val url = context.getString(R.string.api_base_url)
        return url.trim().trimEnd('/')
    }
}
