
package com.example.myapplication.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentForgotBinding
import com.example.myapplication.data.UserStore
import com.google.android.material.snackbar.Snackbar

class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentForgotBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentForgotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnReset.setOnClickListener {
            val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
            val newPass = binding.inputNewPassword.text?.toString().orEmpty()
            val confirm = binding.inputConfirmNew.text?.toString().orEmpty()

            var ok = true
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { binding.inputEmailLayout.error = "E-mail inválido"; ok = false } else binding.inputEmailLayout.error = null
            if (newPass.length < 8) { binding.inputNewPasswordLayout.error = "Mínimo 8 caracteres"; ok = false } else binding.inputNewPasswordLayout.error = null
            if (newPass != confirm) { binding.inputConfirmNewLayout.error = "Senhas não conferem"; ok = false } else binding.inputConfirmNewLayout.error = null
            if (!ok) return@setOnClickListener

            val store = UserStore(requireContext())
            if (store.resetPassword(email, newPass)) {
                Snackbar.make(binding.root, "Senha alterada! Faça login.", Snackbar.LENGTH_LONG).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Snackbar.make(binding.root, "E-mail não encontrado.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
