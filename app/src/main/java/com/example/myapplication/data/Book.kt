package com.example.myapplication.data

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val type: String,        // FISICO | EBOOK | PERIODICO
    val year: Int,
    val language: String,
    val theme: String,
    val edition: String? = null,
    val synopsis: String? = null,
    val coverRes: Int,       // drawable resource id
    val availableCopies: Int = 0,
    val sector: String? = null,
    val shelfCode: String? = null
)