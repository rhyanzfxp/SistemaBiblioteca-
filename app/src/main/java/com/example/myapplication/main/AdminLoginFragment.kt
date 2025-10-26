package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class AdminLoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_admin_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val userField = view.findViewById<TextInputEditText>(R.id.inputAdminUser)
        val passField = view.findViewById<TextInputEditText>(R.id.inputAdminPassword)
        val btnLogin = view.findViewById<MaterialButton>(R.id.btnAdminLogin)
        val btnBack  = view.findViewById<MaterialButton>(R.id.btnBack)

        btnLogin.setOnClickListener {
            val user = userField.text?.toString()?.trim().orEmpty()
            val pass = passField.text?.toString()?.trim().orEmpty()

            // mock simples
            if (user == "admin" && pass == "1234") {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.auth_host, AdminDashboardFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                Snackbar.make(view, "Credenciais incorretas", Snackbar.LENGTH_LONG).show()
            }
        }

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }
}
