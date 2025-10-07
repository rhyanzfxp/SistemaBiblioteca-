package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnGoSearch.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.example.myapplication.R.id.main_host, SearchFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.btnGoProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.example.myapplication.R.id.main_host, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
