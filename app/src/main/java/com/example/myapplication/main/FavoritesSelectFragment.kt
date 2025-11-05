package com.example.myapplication.main

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import android.content.res.ColorStateList
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.RemoteFavoritesStore
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.BookDto
import com.example.myapplication.net.Http
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class FavoritesSelectFragment : Fragment() {

    private val api by lazy { Http.retrofit(requireContext()).create(ApiService::class.java) }
    private val favs by lazy { RemoteFavoritesStore(requireContext()) }

    private lateinit var rv: RecyclerView
    private lateinit var adapter: BooksToggleAdapter

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_favorites_select, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        view.findViewById<MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        rv = view.findViewById(R.id.rvBooksToggle)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = BooksToggleAdapter(
            isFav = { id -> viewLifecycleOwner.lifecycleScope.launch { favs.isFavorite(id) }.let { false } },
            onToggle = { book -> toggleFav(book) },
            onOpen = { book ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, BookDetailsFragment.newInstance(book._id))
                    .addToBackStack(null)
                    .commit()
            }
        )
        rv.adapter = adapter

        load()
    }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val books = api.listBooks()
                // aquece o cache de favoritos
                favs.list()
                adapter.submit(books)
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Erro ao carregar livros: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun toggleFav(book: BookDto) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val now = favs.toggle(book._id)
                Snackbar.make(requireView(),
                    if (now) "Adicionado aos favoritos" else "Removido dos favoritos",
                    Snackbar.LENGTH_SHORT
                ).show()
                adapter.notifyItemChanged(adapter.indexOf(book._id))
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Falha ao atualizar favorito: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    // ---------- Adapter (usa item_book_toggle.xml) ----------
    private class BooksToggleAdapter(
        private val isFav: suspend (String) -> Boolean,
        private val onToggle: (BookDto) -> Unit,
        private val onOpen: (BookDto) -> Unit
    ) : RecyclerView.Adapter<BooksToggleAdapter.VH>() {

        private val data = mutableListOf<BookDto>()
        private val favCache = mutableMapOf<String, Boolean>()

        fun submit(list: List<BookDto>) {
            data.clear(); data.addAll(list); notifyDataSetChanged()
        }

        fun indexOf(id: String) = data.indexOfFirst { it._id == id }

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val capa: ImageView = v.findViewById(R.id.imgCapa)
            val titulo: TextView = v.findViewById(R.id.txtTitulo)
            val autor: TextView = v.findViewById(R.id.txtAutor)
            val btnFav: ImageView = v.findViewById(R.id.btnFav)
        }

        override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
            val v = LayoutInflater.from(p.context).inflate(R.layout.item_book_toggle, p, false)
            return VH(v)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(h: VH, pos: Int) {
            val b = data[pos]
            h.titulo.text = b.title
            h.autor.text = b.author
            // (Opcional) carregar imagem em h.capa se tiver URL

            // Tinta do coração = vermelho se favorito, neutro se não
            val ctx = h.itemView.context
            fun render(isFavLocal: Boolean) {
                val red = ContextCompat.getColor(ctx, R.color.fav_red)
                val neutral = MaterialColors.getColor(h.btnFav, com.google.android.material.R.attr.colorOnSurfaceVariant)
                ImageViewCompat.setImageTintList(h.btnFav, ColorStateList.valueOf(if (isFavLocal) red else neutral))
            }

            val cached = favCache[b._id]
            if (cached != null) {
                render(cached)
            } else {
                // carrega async sem piscar UI
                h.itemView.post {
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                        val v = try { isFav(b._id) } catch (_: Exception) { false }
                        favCache[b._id] = v
                        render(v)
                    }
                }
            }

            h.btnFav.setOnClickListener { onToggle(b) }
            h.itemView.setOnClickListener { onOpen(b) }
        }
    }
}
