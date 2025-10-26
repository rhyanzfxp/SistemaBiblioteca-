package com.example.myapplication.main

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import android.content.res.ColorStateList
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.MaterialColors

data class Livro(
    val id: Int,
    val titulo: String,
    val autor: String,
    val capaResId: Int = R.mipmap.ic_launcher
)

class FavoritesSelectFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: BooksToggleAdapter

    // Catálogo mock (substitua pela sua fonte real quando quiser)
    private val catalogo = listOf(
        Livro(1, "Clean Code", "Robert C. Martin"),
        Livro(2, "Estruturas de Dados em Kotlin", "Loiane Groner"),
        Livro(3, "O Programador Pragmático", "Andrew Hunt & David Thomas"),
        Livro(4, "Algoritmos", "Sedgewick & Wayne")
    )

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_favorites_select, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)

        view.findViewById<MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        rv = view.findViewById(R.id.rvBooksToggle)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = BooksToggleAdapter(
            onToggle = { livro ->
                FavoriteStore.toggle(requireContext(), livro.id)
                adapter.notifyItemChanged(catalogo.indexOf(livro))
            },
            onClick = { /* abrir detalhes se quiser também aqui */ }
        )
        rv.adapter = adapter
        adapter.submit(catalogo)
    }

    /* ---------- Adapter (toggle por cor) ---------- */
    private class BooksToggleAdapter(
        private val onToggle: (Livro) -> Unit,
        private val onClick: (Livro) -> Unit
    ) : RecyclerView.Adapter<BooksToggleAdapter.VH>() {

        private val data = mutableListOf<Livro>()

        fun submit(list: List<Livro>) {
            data.clear()
            data.addAll(list)
            notifyDataSetChanged()
        }

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

        override fun onBindViewHolder(h: VH, pos: Int) {
            val livro = data[pos]
            val ctx = h.itemView.context

            h.capa.setImageResource(livro.capaResId)
            h.titulo.text = livro.titulo
            h.autor.text = livro.autor

            // Ícone fixo: sempre ic_favorite
            h.btnFav.setImageResource(R.drawable.ic_favorite)

            // 🔴 Vermelho quando favorito, cinza quando não
            val isFav = FavoriteStore.isFavorite(ctx, livro.id)
            val red = ContextCompat.getColor(ctx, R.color.fav_red) // cor customizada
            val neutral = MaterialColors.getColor(
                h.btnFav,
                com.google.android.material.R.attr.colorOnSurfaceVariant
            )

            val tint = if (isFav) red else neutral
            ImageViewCompat.setImageTintList(h.btnFav, ColorStateList.valueOf(tint))

            h.btnFav.setOnClickListener { onToggle(livro) }
            h.itemView.setOnClickListener { onClick(livro) }
        }

        override fun getItemCount() = data.size
    }
}
