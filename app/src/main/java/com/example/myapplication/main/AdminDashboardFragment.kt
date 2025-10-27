package com.example.myapplication.main

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.auth.LoginFragment
import com.example.myapplication.data.UserStore
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar

class AdminDashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_admin_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Painel Administrativo"
        toolbar.navigationIcon = null
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.menu_admin_dashboard)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> { confirmLogout(); true }
                else -> false
            }
        }


        val store = UserStore(requireContext())
        val perfil = store.currentUser()?.perfil ?: "aluno"
        if (perfil != "admin") {
            Snackbar.make(view, "Acesso permitido somente para Administrador.", Snackbar.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }

        // navegação para as 3 telas
        view.findViewById<View>(R.id.cardBooks).setOnClickListener { open(AdminBooksFragment()) }
        view.findViewById<View>(R.id.cardUsers).setOnClickListener { open(AdminUsersFragment()) }
        view.findViewById<View>(R.id.cardLoans).setOnClickListener { open(AdminLoansFragment()) }
    }

    private fun open(f: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.auth_host, f)
            .addToBackStack(null)
            .commit()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sair da conta")
            .setMessage("Deseja realmente fazer logoff?")
            .setPositiveButton("Sim") { _, _ ->
                // limpa sessão
                val store = UserStore(requireContext())
                store.logout()
                val prefs = requireContext().getSharedPreferences("session", android.content.Context.MODE_PRIVATE)
                prefs.edit().clear().apply()

                // volta ao login
                parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.auth_host, LoginFragment())
                    .commit()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
