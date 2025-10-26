package com.example.myapplication.main

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class AdminBooksFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_admin_list, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Gerenciar Livros"
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        val rv = view.findViewById<RecyclerView>(R.id.rvList)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = SimpleAdapter(
            listOf(
                "Clean Code" to "Robert C. Martin • Estante A1",
                "Algoritmos" to "Sedgewick & Wayne • Estante B3",
                "Refactoring" to "Martin Fowler • Estante A5",
                "Banco de Dados" to "C. J. Date • Estante D1"
            )
        )

        view.findViewById<FloatingActionButton>(R.id.fabAction).apply {
            show()
            setOnClickListener {
                Snackbar.make(view, "Ação: Adicionar livro (mock)", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}

/** Adapter simples para 2 linhas (título/subtítulo) */
class SimpleAdapter(private val items: List<Pair<String, String>>) :
    RecyclerView.Adapter<SimpleAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvTitle)
        val subtitle: TextView = v.findViewById(R.id.tvSubtitle)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_admin_row, p, false))

    override fun onBindViewHolder(h: VH, i: Int) {
        val (t, s) = items[i]
        h.title.text = t
        h.subtitle.text = s
    }

    override fun getItemCount() = items.size
}
