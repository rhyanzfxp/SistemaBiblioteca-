package com.example.myapplication.main

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.UserStore
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar

class AdminUsersFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_admin_list, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Gerenciar Usuários"
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        val store = UserStore(requireContext())
        if (store.currentUser()?.perfil != "admin") {
            Snackbar.make(view, "Acesso permitido somente para Administrador.", Snackbar.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvList)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = object : RecyclerView.Adapter<VH>() {
            private val data = store.allUsers().toMutableList()

            override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
                VH(LayoutInflater.from(p.context).inflate(R.layout.item_admin_row, p, false))

            override fun onBindViewHolder(h: VH, i: Int) {
                val u = data[i]
                h.title.text = u.name
                h.subtitle.text = "${u.email} • ${if (u.active) "ativo" else "bloqueado"} • perfil=${u.perfil}"

                h.itemView.setOnClickListener {
                    // alterna bloqueio (RF21)
                    store.setActive(u.email, !u.active)
                    Snackbar.make(view, if (u.active) "Usuário desativado" else "Usuário reativado", Snackbar.LENGTH_SHORT).show()
                    data[i] = store.allUsers().first { it.email.equals(u.email, true) }
                    notifyItemChanged(i)
                }

                h.itemView.setOnLongClickListener {
                    // excluir (RF21)
                    store.delete(u.email)
                    data.removeAt(i); notifyItemRemoved(i)
                    Snackbar.make(view, "Usuário excluído", Snackbar.LENGTH_SHORT).show()
                    true
                }
            }

            override fun getItemCount() = data.size
        }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvTitle)
        val subtitle: TextView = v.findViewById(R.id.tvSubtitle)
    }
}
