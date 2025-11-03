package com.example.myapplication.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.FragmentForgotBinding
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.ForgotRequest
import com.example.myapplication.net.Http
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {

    private var _b: FragmentForgotBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentForgotBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val api = Http.retrofit(requireContext()).create(ApiService::class.java)

        b.btnReset.setOnClickListener {
            val email = b.inputEmail.text?.toString()?.trim().orEmpty()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Snackbar.make(b.root, "E-mail inválido", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            b.btnReset.isEnabled = false
            lifecycleScope.launch {
                try {
                    api.forgot(ForgotRequest(email))
                    Snackbar.make(
                        b.root,
                        "Se existir, enviaremos um link de redefinição por e-mail.",
                        Snackbar.LENGTH_LONG
                    ).show()
                    parentFragmentManager.popBackStack()
                } catch (e: Exception) {
                    Snackbar.make(
                        b.root,
                        "Falha ao solicitar redefinição: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                } finally {
                    b.btnReset.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
