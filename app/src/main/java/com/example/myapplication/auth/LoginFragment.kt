package com.example.myapplication.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.UserStore
import com.example.myapplication.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {
    private var _b: FragmentLoginBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentLoginBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val store = UserStore(requireContext())

        b.btnLogin.setOnClickListener {
            val email = b.inputEmail.text?.toString()?.trim().orEmpty()
            val pass  = b.inputPassword.text?.toString()?.trim().orEmpty()

            if (email.isEmpty() || pass.isEmpty()) {
                Snackbar.make(b.root, "Preencha todos os campos", Snackbar.LENGTH_LONG).show() // RF01.3
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Snackbar.make(b.root, "E-mail inválido", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!store.validateLogin(email, pass)) {
                Snackbar.make(b.root, "Credenciais inválidas", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Salva sessão para saudação da Home (RF05)
            run {
                val u = store.findUser(email)
                val prefs = requireContext().getSharedPreferences("session", android.content.Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("user_email", email)
                    .putString("user_name", u?.name ?: "")
                    .apply()
            }

            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }

        b.btnGoRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_host, RegisterFragment())
                .addToBackStack(null).commit()
        }

        b.linkForgot.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_host, ForgotPasswordFragment())
                .addToBackStack(null).commit()
        }
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}
