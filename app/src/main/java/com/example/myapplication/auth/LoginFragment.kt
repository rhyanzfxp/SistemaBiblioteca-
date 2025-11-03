package com.example.myapplication.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.example.myapplication.net.*
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _b: FragmentLoginBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _b = FragmentLoginBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val api = Http.retrofit(requireContext()).create(ApiService::class.java)
        val session = SessionStore(requireContext())

        b.btnLogin.setOnClickListener {
            val email = b.inputEmail.text.toString().trim()
            val pass  = b.inputPassword.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Snackbar.make(b.root, "E-mail inválido", Snackbar.LENGTH_LONG).show(); return@setOnClickListener
            }
            if (pass.length < 3) {
                Snackbar.make(b.root, "Senha muito curta", Snackbar.LENGTH_LONG).show(); return@setOnClickListener
            }

            b.btnLogin.isEnabled = false
            lifecycleScope.launch {
                try {
                    val resp = api.login(AuthRequest(email, pass))

                    session.save(
                        token = resp.token,
                        id    = resp.user.id,
                        name  = resp.user.name,
                        email = resp.user.email,
                        role  = resp.user.role
                    )

                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                } catch (e: Exception) {
                    Snackbar.make(b.root, "Falha no login: ${e.message}", Snackbar.LENGTH_LONG).show()
                } finally {
                    b.btnLogin.isEnabled = true
                }
            }
        }

        b.btnGoRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.example.myapplication.R.id.auth_host, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        b.btnAdminAccess.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.example.myapplication.R.id.auth_host, AdminLoginFragment())
                .addToBackStack(null)
                .commit()
        }
        b.linkForgot.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.example.myapplication.R.id.auth_host, ForgotPasswordFragment())
                .addToBackStack(null)
                .commit()
        }

    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
