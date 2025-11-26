package com.example.myapplication.core

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.myapplication.R
import com.example.myapplication.net.ApiConfig

fun ImageView.loadCover(coverUrl: String?) {
    val context = this.context
    val baseUrl = ApiConfig.baseUrl(context)
    val fullUrl = if (coverUrl.isNullOrBlank()) null else "$baseUrl$coverUrl"

    Glide.with(context)
        .load(fullUrl)
        .placeholder(R.drawable.ic_book_placeholder)
        .error(R.drawable.ic_book_placeholder)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}
