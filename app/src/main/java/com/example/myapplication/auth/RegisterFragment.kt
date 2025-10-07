package com.example.myapplication.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentRegisterBinding
import com.example.myapplication.data.UserStore
import com.google.android.material.snackbar.Snackbar
import com.example.myapplication.R

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // ✅ o link de login deve ser configurado aqui, fora do btnCreate
        binding.linkLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_host, LoginFragment())
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit()
        }

        binding.btnCreate.setOnClickListener {
            val name = binding.inputName.text?.toString()?.trim().orEmpty()
            val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
            val pass = binding.inputPassword.text?.toString().orEmpty()
            val confirm = binding.inputConfirm.text?.toString().orEmpty()

            var ok = true
            if (name.length < 3) { binding.inputNameLayout.error = "Nome muito curto"; ok = false } else binding.inputNameLayout.error = null
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { binding.inputEmailLayout.error = "E-mail inválido"; ok = false } else binding.inputEmailLayout.error = null
            if (pass.length < 8 || !pass.any { it.isDigit() } || !pass.any { it.isLetter() }) { binding.inputPasswordLayout.error = "Senha fraca (8+, letras e números)"; ok = false } else binding.inputPasswordLayout.error = null
            if (pass != confirm) { binding.inputConfirmLayout.error = "Senhas não conferem"; ok = false } else binding.inputConfirmLayout.error = null

            if (!ok) return@setOnClickListener

            val store = UserStore(requireContext())
            if (store.createUser(name, email, pass)) {
                Snackbar.make(binding.root, "Conta criada! Faça login.", Snackbar.LENGTH_LONG).show()
                parentFragmentManager.popBackStack()
            } else {
                Snackbar.make(binding.root, "E-mail já cadastrado.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
