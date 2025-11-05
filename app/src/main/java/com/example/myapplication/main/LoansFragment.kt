package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.Http
import com.example.myapplication.net.LoanDto
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoansFragment : Fragment() {

    private val api by lazy { Http.retrofit(requireContext()).create(ApiService::class.java) }

    private lateinit var rv: RecyclerView
    private lateinit var empty: LinearLayout
    private lateinit var tg: MaterialButtonToggleGroup
    private var allItems: List<LoanDto> = emptyList()

    private enum class Filter { ATIVOS, ATRASADOS, HISTORICO }
    private var current = Filter.ATIVOS

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        s: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_loans, container, false)

        v.findViewById<MaterialToolbar>(R.id.toolbar).apply {
            this.title = "Empréstimos"
            setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        }


        tg = v.findViewById(R.id.tgFilters)
        rv = v.findViewById(R.id.rvLoans)
        empty = v.findViewById(R.id.emptyState)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = LoanAdapter(emptyList())


        tg.check(R.id.btnAtivos)
        tg.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            current = when (checkedId) {
                R.id.btnAtivos -> Filter.ATIVOS
                R.id.btnAtrasados -> Filter.ATRASADOS
                else -> Filter.HISTORICO
            }
            applyFilter()
        }

        load()
        return v
    }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {

                allItems = api.myLoans(false)
                applyFilter()
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Erro ao carregar: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun applyFilter() {
        val list = when (current) {
            Filter.ATIVOS -> allItems.filter { it.status == "APROVADO" || it.status == "RENOVADO" }
            Filter.ATRASADOS -> allItems.filter { it.status == "APROVADO" || it.status == "RENOVADO" }
                .filter { dto ->
                    val due = dto.dueDate?.take(10)

                    due != null
                }
            Filter.HISTORICO -> allItems.filter { it.status == "DEVOLVIDO" || it.status == "NEGADO" }
        }

        (rv.adapter as LoanAdapter).submit(list)
        empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    // ------------ Adapter / ViewHolder --------------

    inner class LoanAdapter(
        private var data: List<LoanDto>
    ) : RecyclerView.Adapter<LoanVH>() {

        fun submit(list: List<LoanDto>) {
            data = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanVH {
            val v = layoutInflater.inflate(R.layout.item_loan, parent, false)
            return LoanVH(v)
        }

        override fun onBindViewHolder(holder: LoanVH, position: Int) = holder.bind(data[position])

        override fun getItemCount() = data.size
    }

    inner class LoanVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle = v.findViewById<android.widget.TextView>(R.id.tvTitle)
        private val tvDates = v.findViewById<android.widget.TextView>(R.id.tvDates)
        private val chip = v.findViewById<Chip>(R.id.chipStatus)
        private val btnRenew = v.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRenew)
        private val btnReturn = v.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReturn)

        fun bind(l: LoanDto) {
            tvTitle.text = l.bookId?.title ?: "Livro"
            val start = l.startDate?.take(10) ?: "--/--/----"
            val due = l.dueDate?.take(10) ?: "--/--/----"
            tvDates.text = "De $start a $due"

            chip.text = l.status


            btnRenew.visibility = View.GONE
            btnReturn.visibility = View.GONE
        }
    }
}
