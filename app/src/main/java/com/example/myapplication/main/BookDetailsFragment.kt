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
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.BookRepository
import com.example.myapplication.data.RemoteFavoritesStore
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.Http
import com.example.myapplication.net.RequestLoanBody
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import com.example.myapplication.core.loadCover

class BookDetailsFragment : Fragment() {

    companion object {
        private const val ARG_ID = "id"
        fun newInstance(id: String): BookDetailsFragment =
            BookDetailsFragment().apply { arguments = Bundle().apply { putString(ARG_ID, id) } }
    }

    private val api by lazy { Http.retrofit(requireContext()).create(ApiService::class.java) }
    private val favs by lazy { RemoteFavoritesStore(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_book_details, container, false)
        val id = arguments?.getString(ARG_ID) ?: return root

        val repo = BookRepository(requireContext())
        val book = repo.byId(id) ?: return root

        val img = root.findViewById<ImageView>(R.id.imgCover)
        val tvTitle = root.findViewById<TextView>(R.id.tvTitle)
        val tvAuthor = root.findViewById<TextView>(R.id.tvAuthor)
        val tvEdition = root.findViewById<TextView>(R.id.tvEdition)
        val tvAvailability = root.findViewById<TextView>(R.id.tvAvailability)
        val tvLocation = root.findViewById<TextView>(R.id.tvLocation)
        val tvSynopsis = root.findViewById<TextView>(R.id.tvSynopsis)
        val btnFav = root.findViewById<Button>(R.id.btnFavorite)
        val btnLoan = root.findViewById<Button>(R.id.btnLoan)
        val btnMap = root.findViewById<Button>(R.id.btnMap)

        img.loadCover(book.coverUrl)
        tvTitle.text = book.title
        tvAuthor.text = "Autor: ${book.author}"
        tvEdition.text = "Edição: ${book.edition ?: "-"}"
        tvAvailability.text = if (book.availableCopies > 0)
            "Disponibilidade: ${book.availableCopies} unidade(s)" else "Indisponível no momento"
        tvLocation.text = "Localização: ${book.sector ?: "-"} / ${book.shelfCode ?: "-"}"
        tvSynopsis.text = "Sinopse:\n${book.synopsis ?: "Sem sinopse disponível."}"


        fun setFavUi(isFav: Boolean) {
            btnFav.text = if (isFav) "Remover Favorito" else "Favoritar"
            btnFav.isEnabled = true
        }

        btnFav.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                setFavUi(favs.isFavorite(book.id))
            } catch (e: Exception) {
                setFavUi(false)
                Snackbar.make(root, "Falha ao consultar favoritos: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }

        btnFav.setOnClickListener {
            btnFav.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val nowFav = favs.toggle(book.id)
                    setFavUi(nowFav)
                    Snackbar.make(
                        root,
                        if (nowFav) "Salvo em favoritos" else "Removido dos favoritos",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    btnFav.isEnabled = true
                    Snackbar.make(root, "Erro ao atualizar favorito: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        btnLoan.setOnClickListener {
            btnLoan.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    api.requestLoan(RequestLoanBody(book.id))
                    Toast.makeText(requireContext(), "Solicitação enviada. Aguarde avaliação.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Snackbar.make(root, "Falha ao solicitar: ${e.message}", Snackbar.LENGTH_LONG).show()
                } finally {
                    btnLoan.isEnabled = true
                }
            }
        }


        btnMap.setOnClickListener {
            val fragment = MapFragment().apply {
                arguments = Bundle().apply {
                    putString("bookId", book.id)
                }
            }
            (requireActivity() as MainActivity).open(fragment)
        }

        return root
    }
}
