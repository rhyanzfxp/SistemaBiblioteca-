package com.example.myapplication.main
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.data.BookRepository

class MapFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_map, container, false)
        val info = v.findViewById<TextView>(R.id.tvShelfInfo)

        // If there is a selected book in arguments, show its shelf
        val bookId = arguments?.getString("bookId")
        bookId?.let {
            val b = BookRepository(requireContext()).byId(it)
            b?.let { info.text = "Livro: ${'$'}{b.title} • Estante: ${'$'}{b.shelfCode} (${b.sector})" }
        }
        return v
    }
}