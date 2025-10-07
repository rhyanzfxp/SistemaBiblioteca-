package com.example.myapplication.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentForgotBinding
import com.google.android.material.snackbar.Snackbar
import com.example.myapplication.R

class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentForgotBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentForgotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Voltar (mesmo efeito do back)
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Voltar ao login explicitamente
        binding.btnGoLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_host, LoginFragment())
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit()
        }

        // Enviar link
        binding.btnSend.setOnClickListener {
            val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.inputEmailLayout.error = "E-mail inválido"
                return@setOnClickListener
            } else binding.inputEmailLayout.error = null

            // TODO: chamar sua API de recuperação aqui
            Snackbar.make(binding.root, "Se existir uma conta, enviaremos o link para $email", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
