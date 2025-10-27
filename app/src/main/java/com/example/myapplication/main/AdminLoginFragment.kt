package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.data.UserStore
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class AdminLoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_admin_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val userField = view.findViewById<TextInputEditText>(R.id.inputAdminUser)
        val passField = view.findViewById<TextInputEditText>(R.id.inputAdminPassword)
        val btnLogin  = view.findViewById<MaterialButton>(R.id.btnAdminLogin)
        val btnBack   = view.findViewById<MaterialButton>(R.id.btnBack)

        val store = UserStore(requireContext())
        // garante que o admin padrão exista (admin@local / admin123)
        store.ensureAdmin()

        btnLogin.setOnClickListener {
            val rawUser = userField.text?.toString()?.trim().orEmpty()
            val pass    = passField.text?.toString()?.trim().orEmpty()

            if (rawUser.isEmpty() || pass.isEmpty()) {
                Snackbar.make(view, "Preencha usuário e senha.", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Se o usuário digitar "admin", mapeia para o e-mail do admin padrão
            val email = if (!rawUser.contains("@") && rawUser.equals("admin", ignoreCase = true))
                "admin@local" else rawUser

            val ok = store.login(email, pass)
            if (!ok) {
                Snackbar.make(view, "Credenciais inválidas ou conta inativa.", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val u = store.currentUser()
            if (u?.perfil != "admin") {
                // não é administrador → bloqueia acesso ao painel
                store.logout()
                Snackbar.make(view, "Acesso permitido somente para Administrador.", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // ok: é admin → abre o painel
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_host, AdminDashboardFragment())
                .addToBackStack(null)
                .commit()
        }

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }
}
