package com.example.myapplication.main

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.Http
import com.example.myapplication.net.LoanDto
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RenovarFragment : Fragment() {

    private val api by lazy { Http.retrofit(requireContext()).create(ApiService::class.java) }
    private lateinit var rv: RecyclerView
    private lateinit var empty: LinearLayout

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val v = i.inflate(R.layout.fragment_loans, c, false)

        v.findViewById<MaterialToolbar>(R.id.toolbar).apply {
            this.title = "Renovar"           // evita o erro de 'title'
            setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        }
        v.findViewById<View>(R.id.tgFilters)?.visibility = View.GONE

        rv = v.findViewById(R.id.rvLoans)
        empty = v.findViewById(R.id.emptyState)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = LoanAdapter(emptyList()) { loan -> renew(loan) }

        load()
        return v
    }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val items = api.myLoans(true) // ativos
                (rv.adapter as LoanAdapter).submit(items)
                empty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Erro ao carregar: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun renew(loan: LoanDto) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                api.adminRenew(loan._id, mapOf("days" to 7))
                Toast.makeText(requireContext(), "Livro renovado por +7 dias", Toast.LENGTH_SHORT).show()
                load()
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Erro ao renovar: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    inner class LoanAdapter(
        private var data: List<LoanDto>,
        private val onRenew: (LoanDto) -> Unit
    ) : RecyclerView.Adapter<LoanVH>() {

        fun submit(list: List<LoanDto>) { data = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(p: ViewGroup, vt: Int): LoanVH {
            val v = layoutInflater.inflate(R.layout.item_loan, p, false)
            return LoanVH(v)
        }

        override fun onBindViewHolder(h: LoanVH, pos: Int) = h.bind(data[pos], onRenew)
        override fun getItemCount() = data.size
    }

    inner class LoanVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
        private val tvDates = v.findViewById<TextView>(R.id.tvDates)
        private val chip = v.findViewById<Chip>(R.id.chipStatus)
        private val btnRenew = v.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRenew)
        private val btnReturn = v.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReturn)

        fun bind(l: LoanDto, onRenew: (LoanDto) -> Unit) {
            tvTitle.text = l.bookId?.title ?: "Livro"
            tvDates.text = "De ${l.startDate?.take(10) ?: "--/--"} a ${l.dueDate?.take(10) ?: "--/--"}"
            chip.text = l.status
            btnReturn.visibility = View.GONE
            btnRenew.visibility = View.VISIBLE
            btnRenew.setOnClickListener { onRenew(l) }
        }
    }
}
