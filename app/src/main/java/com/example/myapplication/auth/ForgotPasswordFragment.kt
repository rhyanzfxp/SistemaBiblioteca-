package com.example.myapplication.auth

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentForgotBinding
import com.google.android.material.snackbar.Snackbar

class ForgotPasswordFragment : Fragment() {
    private var _b: FragmentForgotBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentForgotBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.btnReset.setOnClickListener {
            val email = b.inputEmail.text?.toString()?.trim().orEmpty()

            if (email.isEmpty()) {
                Snackbar.make(b.root, "Preencha todos os campos", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Snackbar.make(b.root, "E-mail inválido", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // RF03.1 — verifica existência
            val prefs = requireContext().getSharedPreferences("users", Context.MODE_PRIVATE)
            val exists = prefs.contains("user_${email.lowercase()}")

            if (!exists) {
                Snackbar.make(b.root, "E-mail não encontrado", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // RF03.2 — feedback informativo
            Snackbar.make(b.root, "Enviamos um link de redefinição para seu e-mail.", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
