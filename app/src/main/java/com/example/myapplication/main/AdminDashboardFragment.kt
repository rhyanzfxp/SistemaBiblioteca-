package com.example.myapplication.main

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.auth.LoginFragment
import com.example.myapplication.net.SessionStore
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


        val session = SessionStore(requireContext())
        val role = session.role()
        if (role != "admin") {
            Snackbar.make(view, "Acesso apenas para Administrador. (role=$role)", Snackbar.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }

        // (Opcional) saudação dinâmica, se tiver tvGreeting no layout
        val tvGreetingId = resources.getIdentifier("tvGreeting", "id", requireContext().packageName)
        if (tvGreetingId != 0) {
            view.findViewById<android.widget.TextView?>(tvGreetingId)?.text =
                "Olá, ${session.name() ?: "Admin"} (${session.role().uppercase()})."
        }


        view.findViewById<View>(R.id.cardBooks).setOnClickListener { open(AdminBooksFragment()) }
        view.findViewById<View>(R.id.cardUsers).setOnClickListener { open(AdminUsersFragment()) }
        view.findViewById<View>(R.id.cardLoans).setOnClickListener { open(AdminLoansFragment()) }

        view.findViewById<View?>(R.id.cardMap)?.setOnClickListener {
            Snackbar.make(view, "Abrir Mapa 2D (plugar seu fragment aqui)", Snackbar.LENGTH_SHORT).show()
        }
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
                // Limpa sessão do backend
                val session = SessionStore(requireContext())
                session.clear()

                // (Se ainda usar UserStore em outras telas, pode manter estas duas linhas.
                //  Caso contrário, pode remover.)
                val legacyPrefs = requireContext().getSharedPreferences("session", android.content.Context.MODE_PRIVATE)
                legacyPrefs.edit().clear().apply()

                // Volta para o Login limpando a pilha
                parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.auth_host, LoginFragment())
                    .commit()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
