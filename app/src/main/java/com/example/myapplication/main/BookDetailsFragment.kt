package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.data.BookRepository
import com.example.myapplication.data.FavoritesStore
import com.example.myapplication.data.UserStore

class BookDetailsFragment : Fragment() {

    companion object {
        private const val ARG_ID = "id"
        fun newInstance(id: String): BookDetailsFragment {
            val f = BookDetailsFragment()
            f.arguments = Bundle().apply { putString(ARG_ID, id) }
            return f
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_book_details, container, false)
        val id = arguments?.getString(ARG_ID) ?: return v

        val repo = BookRepository(requireContext())
        val book = repo.byId(id) ?: return v

        val img = v.findViewById<ImageView>(R.id.imgCover)
        val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
        val tvAuthor = v.findViewById<TextView>(R.id.tvAuthor)
        val tvEdition = v.findViewById<TextView>(R.id.tvEdition)
        val tvAvailability = v.findViewById<TextView>(R.id.tvAvailability)
        val tvLocation = v.findViewById<TextView>(R.id.tvLocation)
        val tvSynopsis = v.findViewById<TextView>(R.id.tvSynopsis)
        val btnFav = v.findViewById<Button>(R.id.btnFavorite)
        val btnLoan = v.findViewById<Button>(R.id.btnLoan)

        img.setImageResource(book.coverRes)
        tvTitle.text = book.title
        tvAuthor.text = "Autor: ${book.author}"

        val ed = book.edition ?: "-"
        val availText = if (book.availableCopies > 0)
            "Disponibilidade: ${book.availableCopies} unidade(s)"
        else "Indisponível no momento"
        val sec = book.sector ?: "-"
        val shelf = book.shelfCode ?: "-"
        val syn = book.synopsis ?: "-"

        tvEdition.text = "Edição: $ed"
        tvAvailability.text = availText
        tvLocation.text = "Localização: $sec / $shelf"
        tvSynopsis.text = "Sinopse:\n$syn"

        val userStore = UserStore(requireContext())
        val current = userStore.currentUserEmail() ?: "guest"
        val favStore = FavoritesStore(requireContext())

        fun refreshFav() {
            btnFav.text = if (favStore.isFavorite(current, book.id)) "Remover Favorito" else "Favoritar"
        }

        refreshFav()

        btnFav.setOnClickListener {
            val added = favStore.toggle(current, book.id)
            Toast.makeText(requireContext(), if (added) "Salvo em favoritos" else "Removido de favoritos", Toast.LENGTH_SHORT).show()
            refreshFav()
        }

        btnLoan.setOnClickListener {
            val msg = if (book.availableCopies > 0)
                "Solicitação enviada. Aguarde confirmação."
            else "Indisponível. Você entrou na fila de espera."
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }

        return v
    }
}
