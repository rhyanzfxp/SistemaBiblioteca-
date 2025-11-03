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
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.Http
import com.example.myapplication.net.RegisterRequest
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val api = Http.retrofit(requireContext()).create(ApiService::class.java)

        val inputName  = view.findViewById<TextInputEditText>(R.id.inputName)
        val inputEmail = view.findViewById<TextInputEditText>(R.id.inputEmail)
        val inputPass  = view.findViewById<TextInputEditText>(R.id.inputPassword)



        val btnRegister = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRegister)
        val btnHaveLogin = view.findViewById<android.widget.TextView?>(R.id.btnHaveLogin)

        btnRegister.setOnClickListener {
            val name  = inputName?.text?.toString()?.trim().orEmpty()
            val email = inputEmail?.text?.toString()?.trim().orEmpty()
            val pass  = inputPass?.text?.toString().orEmpty()

            if (name.length < 3) {
                Snackbar.make(view, "Nome muito curto", Snackbar.LENGTH_LONG).show(); return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Snackbar.make(view, "E-mail inválido", Snackbar.LENGTH_LONG).show(); return@setOnClickListener
            }
            if (pass.length < 3) {
                Snackbar.make(view, "Senha muito curta", Snackbar.LENGTH_LONG).show(); return@setOnClickListener
            }

            btnRegister.isEnabled = false
            lifecycleScope.launch {
                try {
                    api.register(RegisterRequest(name, email, pass))
                    Snackbar.make(view, "Conta criada! Faça login.", Snackbar.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack() // volta pra tela de login
                } catch (e: Exception) {
                    Snackbar.make(view, "Falha no cadastro: ${e.message}", Snackbar.LENGTH_LONG).show()
                } finally {
                    btnRegister.isEnabled = true
                }
            }
        }

        btnHaveLogin?.setOnClickListener { parentFragmentManager.popBackStack() }
    }
}
