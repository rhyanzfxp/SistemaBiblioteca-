package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.MainActivity


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val prefs = requireContext().getSharedPreferences("session", android.content.Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", null) ?: "Visitante"
        binding.tvWelcome.text = "Olá, $userName!"





        binding.etQuickSearch.setOnClickListener {

            (requireActivity() as MainActivity).open(SearchFragment())
        }


        // Mapa 2D
        binding.cardMap.setOnClickListener {
            (requireActivity() as MainActivity).open(MapFragment())
        }

        // Favoritos
        binding.cardFavorites.setOnClickListener {
            (requireActivity() as MainActivity).open(FavoritesFragment())
        }

        // Chatbot
        binding.cardChatbot.setOnClickListener {

        }


        binding.cardChatbot.setOnClickListener {
            (requireActivity() as MainActivity).open(ChatbotFragment())
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}