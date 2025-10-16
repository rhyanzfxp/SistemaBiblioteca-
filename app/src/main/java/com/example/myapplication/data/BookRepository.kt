package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.R

class BookRepository(private val context: Context) {

    private val books: List<Book> by lazy {
        listOf(
            Book(
                id = "1",
                title = "Estruturas de Dados em Kotlin",
                author = "Ana Silva",
                type = "FISICO",
                year = 2022,
                language = "Português",
                theme = "Computação",
                edition = "2ª",
                synopsis = "Uma abordagem moderna de estruturas de dados com exemplos práticos.",
                coverRes = R.drawable.ic_book,
                availableCopies = 3,
                sector = "Tecnologia",
                shelfCode = "TEC-A1"
            ),
            Book(
                id = "2",
                title = "Introdução a Bancos de Dados",
                author = "Carlos Souza",
                type = "FISICO",
                year = 2021,
                language = "Português",
                theme = "Banco de Dados",
                edition = "1ª",
                synopsis = "Conceitos, modelagem e SQL do básico ao avançado.",
                coverRes = R.drawable.ic_book,
                availableCopies = 0,
                sector = "Tecnologia",
                shelfCode = "TEC-B3"
            ),
            Book(
                id = "3",
                title = "Clean Code",
                author = "Robert C. Martin",
                type = "FISICO",
                year = 2008,
                language = "Inglês",
                theme = "Boas Práticas",
                edition = "1st",
                synopsis = "Um manual de artesanato para software.",
                coverRes = R.drawable.ic_book,
                availableCopies = 5,
                sector = "Tecnologia",
                shelfCode = "TEC-A2"
            ),
            Book(
                id = "4",
                title = "História do Ceará",
                author = "João Nunes",
                type = "PERIODICO",
                year = 2019,
                language = "Português",
                theme = "História",
                edition = "Revista 45",
                synopsis = "Edição especial sobre a história do estado.",
                coverRes = R.drawable.ic_book,
                availableCopies = 2,
                sector = "Humanidades",
                shelfCode = "HUM-C2"
            )
        )
    }

    fun search(
        query: String,
        author: String? = null,
        theme: String? = null,
        type: String? = null,
        year: Int? = null,
        language: String? = null
    ): List<Book> {
        return books.filter { b ->
            (query.isBlank() || b.title.contains(query, true) || b.author.contains(query, true)) &&
            (author.isNullOrBlank() || b.author.contains(author!!, true)) &&
            (theme.isNullOrBlank() || b.theme.equals(theme, true)) &&
            (type.isNullOrBlank() || b.type.equals(type, true)) &&
            (year == null || b.year == year) &&
            (language.isNullOrBlank() || b.language.equals(language, true))
        }
    }

    fun byId(id: String): Book? = books.find { it.id == id }

    fun allThemes(): List<String> = books.map { it.theme }.distinct()
    fun allAuthors(): List<String> = books.map { it.author }.distinct()
    fun allYears(): List<Int> = books.map { it.year }.distinct().sortedDescending()
    fun allLanguages(): List<String> = books.map { it.language }.distinct()
    fun allTypes(): List<String> = listOf("FISICO","EBOOK","PERIODICO")
}