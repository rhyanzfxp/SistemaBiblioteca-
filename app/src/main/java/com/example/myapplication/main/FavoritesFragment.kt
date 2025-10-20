package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.Book
import com.example.myapplication.data.BookRepository
import com.example.myapplication.data.FavoritesStore
import com.example.myapplication.data.UserStore
import com.google.android.material.appbar.MaterialToolbar   // ✅ import da Toolbar

class FavoritesFragment : Fragment() {

    private lateinit var rv: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_favorites, container, false)

        // 🔙 Botão de voltar leva para a Home
        val toolbar = v.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            val home = HomeFragment()
            (requireActivity() as com.example.myapplication.MainActivity).open(home)
        }

        rv = v.findViewById(R.id.rvFavs)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = Adapter(emptyList())
        return v
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        val userEmail = UserStore(requireContext()).currentUserEmail() ?: "guest"
        val favStore = FavoritesStore(requireContext())
        val repo = BookRepository(requireContext())
        val ids = favStore.list(userEmail)
        val list = ids.mapNotNull { repo.byId(it) }
        (rv.adapter as Adapter).submit(list)
    }

    inner class Adapter(private var data: List<Book>) : RecyclerView.Adapter<VH>() {
        fun submit(list: List<Book>) { data = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = layoutInflater.inflate(R.layout.item_search_result, parent, false)
            return VH(v)
        }

        override fun getItemCount() = data.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(data[position])
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
        private val tvAuthor = v.findViewById<TextView>(R.id.tvAuthor)
        private val tvMeta = v.findViewById<TextView>(R.id.tvMeta)
        private val img = v.findViewById<ImageView>(R.id.imgCover)

        fun bind(b: Book) {
            tvTitle.text = b.title
            tvAuthor.text = b.author
            val availability = if (b.availableCopies > 0) "Disponível" else "Indisp."
            tvMeta.text = "${b.type} • ${b.year} • $availability"
            img.setImageResource(b.coverRes)

            itemView.setOnClickListener {
                val f = BookDetailsFragment.newInstance(b.id)
                (requireActivity() as com.example.myapplication.MainActivity).open(f)
            }
        }
    }
}
