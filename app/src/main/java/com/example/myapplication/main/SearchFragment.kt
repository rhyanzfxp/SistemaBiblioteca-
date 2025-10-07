
package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentSearchBinding

class SearchFragment: Fragment() {
    private var _b: FragmentSearchBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentSearchBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}
