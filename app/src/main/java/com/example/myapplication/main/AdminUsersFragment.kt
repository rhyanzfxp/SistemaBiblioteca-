package com.example.myapplication.main

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.example.myapplication.net.*
import kotlinx.coroutines.launch

class AdminUsersFragment : Fragment() {

    private lateinit var api: ApiService
    private lateinit var session: SessionStore
    private val data = mutableListOf<UserItem>()

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_admin_list, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        api = Http.retrofit(requireContext()).create(ApiService::class.java)
        session = SessionStore(requireContext())

        val role = (session.role() ?: "").trim().lowercase()
        if (role != "admin") {
            Snackbar.make(view, "Acesso apenas para Administrador.", Snackbar.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        val rv = view.findViewById<RecyclerView>(R.id.rvList)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = object : RecyclerView.Adapter<VH>() {
            override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
                VH(LayoutInflater.from(p.context).inflate(R.layout.item_admin_user, p, false))

            override fun getItemCount() = data.size

            override fun onBindViewHolder(h: VH, pos: Int) {
                val u = data[pos]
                h.name.text = u.name ?: ""
                h.email.text = "${u.email ?: ""} • ${u.role ?: "user"} • ${if (u.active == true) "ativo" else "inativo"}"

                h.itemView.findViewById<View>(R.id.btnActivate).setOnClickListener {
                    updateActive(u._id, true)
                }
                h.itemView.findViewById<View>(R.id.btnDeactivate).setOnClickListener {
                    updateActive(u._id, false)
                }
                h.itemView.findViewById<View>(R.id.btnDelete).setOnClickListener {
                    deleteUser(u._id)
                }
                h.itemView.findViewById<View>(R.id.btnEdit).setOnClickListener {
                    showEditDialog(u) // <<< modal de edição
                }
            }
        }

        load()
    }

    private fun load() {
        view?.let { Snackbar.make(it, "Carregando usuários…", Snackbar.LENGTH_SHORT).show() }
        lifecycleScope.launch {
            try {
                val page = api.listUsers(page = 1, limit = 200)
                data.clear()
                data.addAll(page.items)
                view?.findViewById<RecyclerView>(R.id.rvList)?.adapter?.notifyDataSetChanged()
            } catch (e: Exception) {
                view?.let { Snackbar.make(it, "Erro ao listar: ${e.message}", Snackbar.LENGTH_LONG).show() }
            }
        }
    }

    private fun updateActive(id: String, active: Boolean) {
        lifecycleScope.launch {
            try {
                api.updateUserStatus(id, UpdateStatusRequest(active))
                load()
            } catch (e: Exception) {
                view?.let { Snackbar.make(it, "Erro ao atualizar status: ${e.message}", Snackbar.LENGTH_LONG).show() }
            }
        }
    }

    private fun editUser(id: String, body: UpdateUserRequest, newActive: Boolean?) {
        lifecycleScope.launch {
            try {
                api.updateUser(id, body)
                if (newActive != null) {
                    api.updateUserStatus(id, UpdateStatusRequest(newActive))
                }
                load()
                view?.let { Snackbar.make(it, "Usuário atualizado.", Snackbar.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                view?.let { Snackbar.make(it, "Erro ao editar: ${e.message}", Snackbar.LENGTH_LONG).show() }
            }
        }
    }

    private fun showEditDialog(user: UserItem) {
        val v = layoutInflater.inflate(R.layout.dialog_edit_user, null)

        val inputName  = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputName)
        val inputEmail = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputEmail)
        val inputPass  = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputPassword)
        val spRole     = v.findViewById<Spinner>(R.id.spRole)
        val checkActive = v.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkActive)

        inputName.setText(user.name ?: "")
        inputEmail.setText(user.email ?: "")
        checkActive.isChecked = (user.active != false)

        // opções de papel
        val roles = listOf("user", "admin")
        spRole.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles)
        val pre = (user.role ?: "user").lowercase()
        spRole.setSelection(roles.indexOf(pre).coerceAtLeast(0))

        AlertDialog.Builder(requireContext())
            .setTitle("Editar usuário")
            .setView(v)
            .setPositiveButton("Salvar") { d, _ ->
                val newName  = inputName.text?.toString()?.trim()
                val newEmail = inputEmail.text?.toString()?.trim()
                val newPass  = inputPass.text?.toString()?.takeIf { !it.isNullOrBlank() }
                val newRole  = roles[spRole.selectedItemPosition]
                val newActive = checkActive.isChecked

                val body = UpdateUserRequest(
                    name = newName,
                    email = newEmail,
                    role = newRole,
                    password = newPass
                )
                editUser(user._id, body, newActive)
                d.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUser(id: String) {
        lifecycleScope.launch {
            try {
                api.deleteUser(id)
                load()
                view?.let { Snackbar.make(it, "Usuário excluído.", Snackbar.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                view?.let { Snackbar.make(it, "Erro ao excluir: ${e.message}", Snackbar.LENGTH_LONG).show() }
            }
        }
    }

    private class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.txtName)
        val email: TextView = v.findViewById(R.id.txtEmail)
    }
}
