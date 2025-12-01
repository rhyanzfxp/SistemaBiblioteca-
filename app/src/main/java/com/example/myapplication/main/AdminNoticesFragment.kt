package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.myapplication.R

import com.example.myapplication.net.Http
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminNoticesFragment : Fragment() {

    private lateinit var etTitle: EditText
    private lateinit var etBody: EditText
    private lateinit var btnSend: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_notices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etTitle = view.findViewById(R.id.etNoticeTitle)
        etBody = view.findViewById(R.id.etNoticeBody)
        btnSend = view.findViewById(R.id.btnSendNotice)

        btnSend.setOnClickListener {
            sendNotice()
        }
    }

    private fun sendNotice() {
        val title = etTitle.text.toString().trim()
        val body = etBody.text.toString().trim()

        if (title.isEmpty() || body.isEmpty()) {
            Snackbar.make(requireView(), "Título e corpo do aviso são obrigatórios.", Snackbar.LENGTH_LONG).show()
            return
        }

        btnSend.isEnabled = false
        btnSend.text = "Enviando..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = Http.api(requireContext())
                val response = api.adminCreateNotice(mapOf("title" to title, "body" to body))
                withContext(Dispatchers.Main) {
                    if (response["ok"] == true) {
                        Snackbar.make(requireView(), "Aviso enviado com sucesso para ${response["delivered"]} usuários!", Snackbar.LENGTH_LONG).show()
                        etTitle.setText("")
                        etBody.setText("")
                    } else {
                        Snackbar.make(requireView(), "Erro ao enviar aviso.", Snackbar.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(requireView(), "Erro de rede: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    btnSend.isEnabled = true
                    btnSend.text = "Enviar Aviso"
                }
            }
        }
    }
}
