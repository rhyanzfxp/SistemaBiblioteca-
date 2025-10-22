package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentChatbotBinding
import com.example.myapplication.databinding.ItemChatMessageBotBinding
import com.example.myapplication.databinding.ItemChatMessageUserBinding

class ChatbotFragment : Fragment() {

    private var _b: FragmentChatbotBinding? = null
    private val b get() = _b!!

    private val adapter by lazy { ChatAdapter() }
    private val messages = mutableListOf<ChatMsg>()

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


        messages += ChatMsg.Bot("Olá! Sou o assistente da biblioteca 🤖")
        messages += ChatMsg.Bot("Posso ajudar com acervo, prazos e uso do app.")
        adapter.submitList(messages.toList())
        scrollToEnd()


        b.chipPrazo.setOnClickListener { quick("Quais são meus prazos?") }
        b.chipBuscar.setOnClickListener { quick("Procurar livro de redes") }
        b.chipComoUsar.setOnClickListener { quick("Como renovar empréstimo?") }


        b.btnSend.setOnClickListener { sendInput() }
        b.input.setOnEditorActionListener { _, _, _ -> sendInput(); true }
    }

    private fun quick(text: String) {
        messages += ChatMsg.User(text)
        messages += mockAnswer(text)
        adapter.submitList(messages.toList())
        scrollToEnd()
    }

    private fun sendInput() {
        val txt = b.input.text?.toString()?.trim().orEmpty()
        if (txt.isEmpty()) return
        b.input.text?.clear()

        messages += ChatMsg.User(txt)
        messages += ChatMsg.Bot("Recebi! Em breve conectaremos a IA ")
        adapter.submitList(messages.toList())
        scrollToEnd()
    }

    private fun mockAnswer(query: String): ChatMsg {
        val ans = when {
            query.contains("prazo", true) ->
                "Você possui 2 empréstimos. Próximo vencimento: 28/10."
            query.contains("renov", true) ->
                "Para renovar: Meus Empréstimos → selecione o item → Renovar."
            query.contains("buscar", true) || query.contains("procurar", true) ->
                "Envie título/autor/assunto que eu pesquiso no acervo 🔎"
            else -> "Certo! Logo teremos respostas mais completas com IA."
        }
        return ChatMsg.Bot(ans)
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
