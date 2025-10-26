package com.example.myapplication.main

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar

class AdminLoansFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_admin_list, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Empréstimos"
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        val loans = listOf(
            "Clean Code → João Victor" to "Vence: 26/10 • status: ativo",
            "Algoritmos → Maria Clara" to "Atrasado: 3 dias • status: atraso",
            "Banco de Dados → Pedro Lima" to "Vence: 28/10 • status: ativo"
        )

        val rv = view.findViewById<RecyclerView>(R.id.rvList)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = object : RecyclerView.Adapter<SimpleAdapter.VH>() {
            override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
                SimpleAdapter.VH(
                    LayoutInflater.from(p.context).inflate(R.layout.item_admin_row, p, false)
                )

            override fun onBindViewHolder(h: SimpleAdapter.VH, i: Int) {
                val (t, s) = loans[i]
                h.title.text = t
                h.subtitle.text = s
                h.itemView.setOnClickListener {
                    Snackbar.make(view, "Ação: ver detalhes/renovar (mock)", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun getItemCount() = loans.size
        }
    }
}
