package com.example.myapplication.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.Http
import com.example.myapplication.net.NotificationDto
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private val api by lazy { Http.retrofit(requireContext()).create(ApiService::class.java) }

    private lateinit var rv: RecyclerView
    private lateinit var adapter: NoticeAdapter
    private var onlyUnread = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_notifications, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        rv = view.findViewById(R.id.rvNotif)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = NoticeAdapter(emptyList()) { notice -> markRead(notice._id) }
        rv.adapter = adapter

        // Filtro: Todos vs Não lidos
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAll).setOnClickListener {
            onlyUnread = false; load()
        }
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnUnread).setOnClickListener {
            onlyUnread = true; load()
        }

        // Menu admin: enviar aviso geral
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.menu_notifications_admin)
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_send_notice) {
                showSendDialog(view)
                true
            } else false
        }

        // back físico
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { parentFragmentManager.popBackStack() }
        })

        load()
    }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val list = api.notifications(onlyUnread)
                adapter.submit(list)
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Erro ao carregar avisos: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun markRead(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                api.markRead(id)
                if (onlyUnread) load()
            } catch (_: Exception) { }
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
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            api.adminCreateNotice(mapOf("title" to t, "body" to m))
                            Snackbar.make(root, "Aviso enviado!", Snackbar.LENGTH_LONG).show()
                            load()
                        } catch (e: Exception) {
                            Snackbar.make(root, "Erro ao enviar aviso: ${e.message}", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                d.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    inner class NoticeAdapter(
        private var data: List<NotificationDto>,
        private val onRead: (NotificationDto) -> Unit
    ) : RecyclerView.Adapter<NoticeVH>() {

        fun submit(list: List<NotificationDto>) { data = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeVH {
            val v = layoutInflater.inflate(R.layout.item_notifications, parent, false)
            return NoticeVH(v)
        }

        override fun onBindViewHolder(holder: NoticeVH, position: Int) {
            holder.bind(data[position], onRead)
        }

        override fun getItemCount() = data.size
    }

    inner class NoticeVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle = v.findViewById<android.widget.TextView>(R.id.tvTitle)
        private val tvSubtitle = v.findViewById<android.widget.TextView>(R.id.tvSubtitle)

        fun bind(n: NotificationDto, onRead: (NotificationDto) -> Unit) {
            tvTitle.text = n.title
            tvSubtitle.text = n.body
            itemView.setOnClickListener { onRead(n) }
        }
    }
}
