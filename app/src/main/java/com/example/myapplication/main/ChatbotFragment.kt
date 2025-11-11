package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.ChatApi
import com.example.myapplication.data.ChatRequest
import com.example.myapplication.databinding.FragmentChatbotBinding
import com.example.myapplication.databinding.ItemChatMessageBotBinding
import com.example.myapplication.databinding.ItemChatMessageUserBinding
import kotlinx.coroutines.launch

class ChatbotFragment : Fragment() {

    private var _b: FragmentChatbotBinding? = null
    private val b get() = _b!!
    private val adapter by lazy { ChatAdapter() }
    private val messages = mutableListOf<ChatMsg>()
    private val api = ChatApi.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentChatbotBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.recycler.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
            adapter = this@ChatbotFragment.adapter
        }

        // Mensagem inicial
        messages += ChatMsg.Bot("Olá! Sou o assistente da biblioteca 🤖")
        messages += ChatMsg.Bot("Posso ajudar com acervo, prazos e uso do app.")
        adapter.submitList(messages.toList())
        scrollToEnd()

        // Chips rápidos
        b.chipPrazo.setOnClickListener { sendQuick("Quais são meus prazos?") }
        b.chipBuscar.setOnClickListener { sendQuick("Procurar livro de redes") }
        b.chipComoUsar.setOnClickListener { sendQuick("Como usar o app?") }

        b.btnSend.setOnClickListener { sendInput() }
        b.input.setOnEditorActionListener { _, _, _ -> sendInput(); true }
    }

    private fun sendQuick(text: String) {
        addUserMessage(text)
        sendToApi(text)
    }

    private fun sendInput() {
        val txt = b.input.text?.toString()?.trim().orEmpty()
        if (txt.isEmpty()) return
        b.input.text?.clear()
        addUserMessage(txt)
        sendToApi(txt)
    }

    private fun addUserMessage(text: String) {
        messages += ChatMsg.User(text)
        adapter.submitList(messages.toList())
        scrollToEnd()
    }

    private fun sendToApi(userMsg: String) {
        messages += ChatMsg.Bot("Digitando resposta...")
        adapter.submitList(messages.toList())
        scrollToEnd()

        lifecycleScope.launch {
            try {
                val res = api.sendMessage(ChatRequest(userMsg))
                val reply = res.reply ?: "Não consegui entender sua pergunta 😅"
                if (messages.isNotEmpty()) messages.removeAt(messages.size - 1) // <-- aqui
                messages += ChatMsg.Bot(reply)
            } catch (e: Exception) {
                if (messages.isNotEmpty()) messages.removeAt(messages.size - 1) // <-- e aqui
                messages += ChatMsg.Bot("❌ Erro ao conectar ao servidor. Verifique a rede.")
            }
            adapter.submitList(messages.toList())
            scrollToEnd()
        }
    }

    private fun scrollToEnd() {
        b.recycler.post {
            b.recycler.smoothScrollToPosition(adapter.itemCount.coerceAtLeast(1) - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}

/* --------- Adapter / ViewHolders --------- */

sealed class ChatMsg {
    data class User(val text: String) : ChatMsg()
    data class Bot(val text: String) : ChatMsg()
}

private object ChatDiff : DiffUtil.ItemCallback<ChatMsg>() {
    override fun areItemsTheSame(oldItem: ChatMsg, newItem: ChatMsg) = oldItem === newItem
    override fun areContentsTheSame(oldItem: ChatMsg, newItem: ChatMsg) = oldItem == newItem
}

private class ChatAdapter : ListAdapter<ChatMsg, RecyclerView.ViewHolder>(ChatDiff) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ChatMsg.User -> R.layout.item_chat_message_user
        is ChatMsg.Bot  -> R.layout.item_chat_message_bot
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_chat_message_user ->
                UserVH(ItemChatMessageUserBinding.inflate(inflater, parent, false))
            else ->
                BotVH(ItemChatMessageBotBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserVH -> holder.bind((getItem(position) as ChatMsg.User).text)
            is BotVH  -> holder.bind((getItem(position) as ChatMsg.Bot).text)
        }
    }

    private class UserVH(private val b: ItemChatMessageUserBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(t: String) { b.txt.text = t }
    }

    private class BotVH(private val b: ItemChatMessageBotBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(t: String) { b.txt.text = t }
    }
}
