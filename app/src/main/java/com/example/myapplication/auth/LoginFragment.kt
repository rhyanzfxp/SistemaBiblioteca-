
package com.example.myapplication.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.FragmentLoginBinding
import com.example.myapplication.data.UserStore
import com.google.android.material.snackbar.Snackbar
import com.example.myapplication.R


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnLogin.setOnClickListener {
            val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
            val pass = binding.inputPassword.text?.toString().orEmpty()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.inputEmailLayout.error = "E-mail inválido"
                return@setOnClickListener
            } else binding.inputEmailLayout.error = null

            if (pass.length < 8) {
                binding.inputPasswordLayout.error = "Mínimo 8 caracteres"
                return@setOnClickListener
            } else binding.inputPasswordLayout.error = null

            val store = UserStore(requireContext())
            if (store.validateLogin(email, pass)) {
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            } else {
                Snackbar.make(binding.root, "Credenciais inválidas", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.linkRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_host, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.linkForgot.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_host, ForgotPasswordFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
