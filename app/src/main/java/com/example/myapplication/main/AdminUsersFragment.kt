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

class AdminUsersFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_admin_list, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Gerenciar Usuários"
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        val data = mutableListOf(
            UserRow("João Victor", "joao@exemplo.com", false),
            UserRow("Maria Clara", "maria@exemplo.com", true),
            UserRow("Pedro Lima", "pedro@exemplo.com", false)
        )

        val rv = view.findViewById<RecyclerView>(R.id.rvList)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = object : RecyclerView.Adapter<SimpleAdapter.VH>() {
            override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
                SimpleAdapter.VH(
                    LayoutInflater.from(p.context).inflate(R.layout.item_admin_row, p, false)
                )

            override fun onBindViewHolder(h: SimpleAdapter.VH, i: Int) {
                val u = data[i]
                h.title.text = u.name
                h.subtitle.text = "${u.email} • ${if (u.blocked) "bloqueado" else "ativo"}"
                h.itemView.setOnClickListener {
                    // alterna bloqueio (mock)
                    data[i] = u.copy(blocked = !u.blocked)
                    notifyItemChanged(i)
                    Snackbar.make(
                        view,
                        if (data[i].blocked) "Usuário bloqueado (mock)" else "Usuário desbloqueado (mock)",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            override fun getItemCount() = data.size
        }
    }
}

data class UserRow(val name: String, val email: String, val blocked: Boolean)
