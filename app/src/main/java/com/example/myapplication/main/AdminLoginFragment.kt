package com.example.myapplication.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.main.AdminDashboardFragment
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.AuthRequest
import com.example.myapplication.net.Http
import com.example.myapplication.net.SessionStore
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AdminLoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_admin_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val api = Http.retrofit(requireContext()).create(ApiService::class.java)
        val session = SessionStore(requireContext())

        val inputEmail = view.findViewById<TextInputEditText>(R.id.inputAdminUser)
        val inputPass  = view.findViewById<TextInputEditText>(R.id.inputAdminPassword)
        val btnLogin   = view.findViewById<Button>(R.id.btnAdminLogin)
        val btnBack    = view.findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        btnLogin.setOnClickListener {
            val email = inputEmail?.text?.toString()?.trim().orEmpty()
            val pass  = inputPass?.text?.toString().orEmpty()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Snackbar.make(view, "E-mail inválido", Snackbar.LENGTH_LONG).show(); return@setOnClickListener
            }
            if (pass.length < 3) {
                Snackbar.make(view, "Senha muito curta", Snackbar.LENGTH_LONG).show(); return@setOnClickListener
            }

            btnLogin.isEnabled = false
            lifecycleScope.launch {
                try {
                    val resp = api.adminLogin(AuthRequest(email, pass))

                    session.save(
                        token = resp.token,
                        id    = resp.user.id,
                        name  = resp.user.name,
                        email = resp.user.email,
                        role  = resp.user.role
                    )

                    val role = (resp.user.role ?: "").trim().lowercase()
                    if (role != "admin") {
                        Snackbar.make(view, "Acesso permitido somente para Administrador.", Snackbar.LENGTH_LONG).show()
                        return@launch
                    }


                    parentFragmentManager.beginTransaction()
                        .replace(R.id.auth_host, AdminDashboardFragment())
                        .addToBackStack(null)
                        .commit()

                } catch (e: Exception) {
                    Snackbar.make(view, "Falha no login: ${e.message}", Snackbar.LENGTH_LONG).show()
                } finally {
                    btnLogin.isEnabled = true
                }
            }
        }
    }
}
