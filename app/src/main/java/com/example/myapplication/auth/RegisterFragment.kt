package com.example.myapplication.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.data.UserStore
import com.example.myapplication.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar

class RegisterFragment : Fragment() {
    private var _b: FragmentRegisterBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentRegisterBinding.inflate(inflater, container, false)
        return b.root
    }

    private fun isStrong(pass: String): Boolean {
        val hasUpper = pass.any { it.isUpperCase() }
        val hasDigit = pass.any { it.isDigit() }
        return pass.length >= 8 && hasUpper && hasDigit
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val store = UserStore(requireContext())

        b.btnRegister.setOnClickListener {
            val name = b.inputName.text?.toString()?.trim().orEmpty()
            val email = b.inputEmail.text?.toString()?.trim().orEmpty()
            val pass  = b.inputPassword.text?.toString()?.trim().orEmpty()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Snackbar.make(b.root, "Preencha todos os campos", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Snackbar.make(b.root, "E-mail inválido", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!isStrong(pass)) {
                Snackbar.make(b.root,
                    "A senha deve ter pelo menos 8 caracteres, incluindo um número e uma letra maiuscula.",
                    Snackbar.LENGTH_LONG
                ).show() // RF02.3 texto exatamente como no requisito
                return@setOnClickListener
            }

            val ok = store.createUser(name, email, pass)
            if (!ok) {
                Snackbar.make(b.root, "O email informado já está cadastrado", Snackbar.LENGTH_LONG).show() // RF02.2
                return@setOnClickListener
            }

            Snackbar.make(b.root, "Conta criada! Faça login.", Snackbar.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
        }

        b.btnHaveLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}
