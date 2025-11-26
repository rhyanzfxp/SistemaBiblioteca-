package com.example.myapplication.main

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.RemoteFavoritesStore
import com.example.myapplication.net.BookDto
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import com.example.myapplication.core.loadCover

class FavoritesFragment : Fragment() {

    private val store by lazy { RemoteFavoritesStore(requireContext()) }

    private lateinit var rv: RecyclerView
    private lateinit var empty: View
    private lateinit var adapter: FavAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_favorites, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        rv = view.findViewById(R.id.rvFavs)
        empty = view.findViewById(R.id.emptyStateFavs)

        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = FavAdapter(
            onToggle = { book -> toggleAndReload(book) }
        )
        rv.adapter = adapter

        load()
    }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val items = store.list()              // GET /me/favorites
                adapter.submit(items)
                empty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Erro ao carregar favoritos: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun toggleAndReload(book: BookDto) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                store.toggle(book._id)                // POST/DELETE no backend
                load()
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Falha ao atualizar: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    // ---------------- Adapter ----------------

    private class FavAdapter(
        private val onToggle: (BookDto) -> Unit
    ) : RecyclerView.Adapter<FavAdapter.VH>() {

        private val data = mutableListOf<BookDto>()

        fun submit(list: List<BookDto>) {
            data.clear()
            data.addAll(list)
            notifyDataSetChanged()
        }

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {

            val img: ImageView = v.findViewById(R.id.imgCapa)
            val title: TextView =
                v.findViewById(R.id.tvTitle) ?: v.findViewById(R.id.txtTitulo)
            val subtitle: TextView =
                v.findViewById(R.id.tvSubtitle) ?: v.findViewById(R.id.txtAutor)

            val btnToggle: View = v.findViewById(R.id.imgToggleFav)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {

            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_favorito, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, position: Int) {
            val b = data[position]
            h.title.text = b.title
            h.subtitle.text = b.author ?: "-"
            h.img.loadCover(b.coverUrl)
            h.btnToggle.setOnClickListener { onToggle(b) }
        }

        override fun getItemCount(): Int = data.size
    }
}
