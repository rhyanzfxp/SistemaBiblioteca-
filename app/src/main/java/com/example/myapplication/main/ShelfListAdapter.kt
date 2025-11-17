package com.example.myapplication.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

data class ShelfItem(
    val shelfCode: String,
    val sector: String?,
    val floor: Floor,
    val bookCount: Int
)

class ShelfListAdapter(
    private val items: List<ShelfItem>,
    private val onClick: (ShelfItem) -> Unit
) : RecyclerView.Adapter<ShelfListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvShelfTitle)
        val tvSubtitle: TextView = itemView.findViewById(R.id.tvShelfSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shelf, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val floorText = when (item.floor) {
            Floor.GROUND -> "Térreo"
            Floor.UPPER -> "Superior"
        }

        holder.tvTitle.text = "Estante ${item.shelfCode}"
        holder.tvSubtitle.text =
            "Setor: ${item.sector ?: "-"} • Andar: $floorText • ${item.bookCount} livro(s)"

        holder.itemView.setOnClickListener { onClick(item) }
    }
}
