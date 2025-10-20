package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.data.BookRepository
import com.google.android.material.appbar.MaterialToolbar   // ✅ Import da Toolbar

class MapFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_map, container, false)

        // ✅ Botão de voltar leva para a tela Home
        val toolbar = v.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            val home = HomeFragment()
            (requireActivity() as com.example.myapplication.MainActivity).open(home)
        }

        val info = v.findViewById<TextView>(R.id.tvShelfInfo)

        // Se houver um livro selecionado, mostra a estante correspondente
        val bookId = arguments?.getString("bookId")
        bookId?.let {
            val b = BookRepository(requireContext()).byId(it)
            b?.let {
                info.text = "Livro: ${b.title} • Estante: ${b.shelfCode} (${b.sector})"
            }
        }

        return v
    }
}
