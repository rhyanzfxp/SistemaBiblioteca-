package com.example.myapplication.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.data.NotificationStore
import com.example.myapplication.data.UserStore
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar

class NotificationsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_notifications, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        // Back físico
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { parentFragmentManager.popBackStack() }
        })

        // RF23: se for admin, mostra ação para enviar aviso geral
        val isAdmin = UserStore(requireContext()).currentUser()?.perfil == "admin"
        if (isAdmin) {
            toolbar.menu.clear()
            toolbar.inflateMenu(R.menu.menu_notifications_admin) // crie um menu simples com 1 item "Enviar"
            toolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.action_send_notice) {
                    showSendDialog(view)
                    true
                } else false
            }
        }
    }

    private fun showSendDialog(root: View) {
        val ctx = requireContext()
        val inputTitle = EditText(ctx).apply { hint = "Título *" }
        val inputMsg = EditText(ctx).apply { hint = "Mensagem *"; minLines = 3 }
        val container = android.widget.LinearLayout(ctx).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 16, 32, 0)
            addView(inputTitle); addView(inputMsg)
        }

        AlertDialog.Builder(ctx)
            .setTitle("Enviar aviso geral")
            .setView(container)
            .setPositiveButton("Enviar") { d, _ ->
                val t = inputTitle.text.toString().trim()
                val m = inputMsg.text.toString().trim()
                if (t.isBlank() || m.isBlank()) {
                    Snackbar.make(root, "Preencha título e mensagem.", Snackbar.LENGTH_LONG).show()
                } else {
                    NotificationStore(ctx).send(t, m) // RF23
                    Snackbar.make(root, "Aviso enviado (mock).", Snackbar.LENGTH_LONG).show()
                }
                d.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
