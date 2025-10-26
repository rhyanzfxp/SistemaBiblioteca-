package com.example.myapplication.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.android.material.appbar.MaterialToolbar

// Modelo simples de livro
data class LivroFavorito(
    val id: Int,
    val titulo: String,
    val autor: String,
    val capaResId: Int = R.mipmap.ic_launcher // placeholder de capa
)

class FavoritesFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var adapter: FavoritosAdapter

    // Catálogo básico (exemplo). Substitua por sua lista real.
    private val catalogo = listOf(
        LivroFavorito(1, "Clean Code", "Robert C. Martin"),
        LivroFavorito(2, "Estruturas de Dados em Kotlin", "Loiane Groner"),
        LivroFavorito(3, "O Programador Pragmático", "Andrew Hunt & David Thomas")
    )

    // SharedPreferences para persistir os IDs favoritos
    private val prefsName = "fav_prefs"
    private val keyIds = "fav_ids"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_favorites, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar com botão voltar
        view.findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            navigateBackOrHome()
        }

        // Botão físico/gesto do Android
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateBackOrHome()
                }
            }
        )

        rv = view.findViewById(R.id.rvFavs)
        emptyState = view.findViewById(R.id.emptyStateFavs)

        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = FavoritosAdapter(onClick = {
            // Aqui você pode abrir os detalhes do livro se quiser
        })
        rv.adapter = adapter

        renderLista()
    }

    /** Volta uma tela se houver back stack; senão retorna à Home */
    private fun navigateBackOrHome() {
        val fm = requireActivity().supportFragmentManager
        if (fm.backStackEntryCount > 0) {
            fm.popBackStack()
        } else {
            fm.beginTransaction()
                .replace(R.id.nav_host_fragment, HomeFragment())
                .commit()
        }
    }

    // ===== Persistência simples (IDs em Set<String>) =====
    private fun getIdsFavoritos(): Set<Int> {
        val sp = requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        return sp.getStringSet(keyIds, emptySet())!!.mapNotNull { it.toIntOrNull() }.toSet()
    }

    // Monta lista a partir do catálogo + IDs salvos
    private fun renderLista() {
        val ids = getIdsFavoritos()
        val favoritos = catalogo.filter { it.id in ids }
        adapter.submitList(favoritos)
        updateEmptyState(favoritos.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rv.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}

/* ===================== ADAPTER (somente leitura) ===================== */

private class FavoritosAdapter(
    private val onClick: (LivroFavorito) -> Unit
) : ListAdapter<LivroFavorito, FavoritosAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<LivroFavorito>() {
        override fun areItemsTheSame(o: LivroFavorito, n: LivroFavorito) = o.id == n.id
        override fun areContentsTheSame(o: LivroFavorito, n: LivroFavorito) = o == n
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val capa: ImageView = view.findViewById(R.id.imgCapa)
        val titulo: TextView = view.findViewById(R.id.txtTitulo)
        val autor: TextView = view.findViewById(R.id.txtAutor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorito, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val livro = getItem(pos)
        h.capa.setImageResource(livro.capaResId)
        h.titulo.text = livro.titulo
        h.autor.text = livro.autor
        h.itemView.setOnClickListener { onClick(livro) }
    }
}
