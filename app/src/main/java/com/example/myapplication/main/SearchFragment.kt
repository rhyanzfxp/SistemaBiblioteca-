package com.example.myapplication.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.Book
import com.example.myapplication.core.loadCover
import com.example.myapplication.data.BookRepository
import com.google.android.material.appbar.MaterialToolbar   // ✅ Import necessário

class SearchFragment : Fragment() {

    private lateinit var repo: BookRepository
    private lateinit var rv: RecyclerView
    private lateinit var et: EditText
    private lateinit var tvFilters: TextView
    private lateinit var btnSearch: ImageButton
    private lateinit var btnFilters: ImageButton

    private var fAuthor: String? = null
    private var fTheme: String? = null
    private var fType: String? = null
    private var fYear: Int? = null
    private var fLanguage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_search, container, false)

        // ✅ Botão de voltar leva para a tela Home
        val toolbar = v.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            val home = HomeFragment()
            (requireActivity() as com.example.myapplication.MainActivity).open(home)
        }

        repo = BookRepository(requireContext())

        rv = v.findViewById(R.id.rvResults)
        et = v.findViewById(R.id.etQuery)
        tvFilters = v.findViewById(R.id.tvActiveFilters)
        btnSearch = v.findViewById(R.id.btnSearch)
        btnFilters = v.findViewById(R.id.btnFilters)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = ResultsAdapter(emptyList())

        btnSearch.setOnClickListener { load() }
        btnFilters.setOnClickListener { showFilters() }

        load()
        return v
    }

    private fun descFilters(): String {
        val parts = mutableListOf<String>()
        fAuthor?.let { parts.add("Autor: $it") }
        fTheme?.let { parts.add("Tema: $it") }
        fType?.let { parts.add("Tipo: $it") }
        fYear?.let { parts.add("Ano: $it") }
        fLanguage?.let { parts.add("Idioma: $it") }
        return if (parts.isEmpty()) "Sem filtros" else parts.joinToString(" • ")
    }

    private fun load() {
        val list = repo.search(
            query = et.text?.toString() ?: "",
            author = fAuthor,
            theme = fTheme,
            type = fType,
            year = fYear,
            language = fLanguage
        )
        (rv.adapter as ResultsAdapter).submit(list)
        tvFilters.text = descFilters()
    }

    private fun showFilters() {
        val options = arrayOf("Autor", "Tema", "Tipo", "Ano", "Idioma", "Limpar filtros")
        AlertDialog.Builder(requireContext())
            .setTitle("Filtros")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pick("Autor", repo.allAuthors()) { fAuthor = it; load() }
                    1 -> pick("Tema", repo.allThemes()) { fTheme = it; load() }
                    2 -> pick("Tipo", repo.allTypes()) { fType = it; load() }
                    3 -> pick("Ano", repo.allYears().map { it.toString() }) { fYear = it.toIntOrNull(); load() }
                    4 -> pick("Idioma", repo.allLanguages()) { fLanguage = it; load() }
                    5 -> { fAuthor = null; fTheme = null; fType = null; fYear = null; fLanguage = null; load() }
                }
            }.show()
    }

    private fun pick(title: String, items: List<String>, onPick: (String) -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setItems(items.toTypedArray()) { _, i -> onPick(items[i]) }
            .show()
    }

    inner class ResultsAdapter(private var data: List<Book>) :
        RecyclerView.Adapter<ResultsVH>() {

        fun submit(list: List<Book>) {
            data = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsVH {
            val v = layoutInflater.inflate(R.layout.item_search_result, parent, false)
            return ResultsVH(v)
        }

        override fun getItemCount() = data.size
        override fun onBindViewHolder(holder: ResultsVH, position: Int) =
            holder.bind(data[position])
    }

    inner class ResultsVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
        private val tvAuthor = v.findViewById<TextView>(R.id.tvAuthor)
        private val tvMeta = v.findViewById<TextView>(R.id.tvMeta)
        private val img = v.findViewById<android.widget.ImageView>(R.id.imgCover)

        fun bind(b: Book) {
            tvTitle.text = b.title
            tvAuthor.text = b.author
            val availability = if (b.availableCopies > 0) "Disponível" else "Indisp."
            tvMeta.text = "${b.type} • ${b.year} • $availability"
            img.loadCover(b.coverUrl)

            itemView.setOnClickListener {
                val f = BookDetailsFragment.newInstance(b.id)
                (requireActivity() as com.example.myapplication.MainActivity).open(f)
            }
        }
    }
}
