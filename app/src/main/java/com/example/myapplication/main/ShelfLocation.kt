package com.example.myapplication.main

import android.graphics.RectF

data class ShelfLocation(
    val shelfCode: String,
    val floor: Floor,
    val rect: RectF
)
