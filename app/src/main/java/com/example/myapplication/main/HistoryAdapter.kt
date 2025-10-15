package com.example.myapplication.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemHistoryBookBinding

class HistoryAdapter(
    private val items: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    inner class VH(val b: ItemHistoryBookBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(t: String) {
            b.tvTitleAuthor.text = t
            b.root.setOnClickListener { onClick(t) }
        }
    }
    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemHistoryBookBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, i: Int) = h.bind(items[i])
    override fun getItemCount() = items.size
}
