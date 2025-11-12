package com.example.myapplication.main

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
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
import java.time.LocalDate

class RenovarFragment : Fragment() {

    private val api by lazy { Http.retrofit(requireContext()).create(ApiService::class.java) }
    private lateinit var rv: RecyclerView
    private lateinit var empty: LinearLayout

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val v = i.inflate(R.layout.fragment_loans, c, false)

        v.findViewById<MaterialToolbar>(R.id.toolbar).apply {
            this.title = "Renovar"
            setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        }
        v.findViewById<View>(R.id.tgFilters)?.visibility = View.GONE

        rv = v.findViewById(R.id.rvLoans)
        empty = v.findViewById(R.id.emptyState)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = LoanAdapter(emptyList()) { loan -> requestRenewal(loan) }

        load()
        return v
    }

    /** Carrega apenas empréstimos ATIVOS e ATRASADOS (dueDate < hoje). */
    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ativos = api.myLoans(true) // APROVADO/RENOVADO
                val hoje = LocalDate.now()
                val atrasados = ativos.filter { l ->
                    val d = l.dueDate?.take(10)
                    try {
                        d != null && LocalDate.parse(d).isBefore(hoje)
                    } catch (_: Exception) {
                        false
                    }
                }
                (rv.adapter as LoanAdapter).submit(atrasados)
                empty.visibility = if (atrasados.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Erro ao carregar: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    /** Envia PEDIDO de renovação para o administrador aprovar. */
    private fun requestRenewal(loan: LoanDto) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Pode parametrizar dias/motivo se desejar (ex.: mapOf("addDays" to 7, "reason" to "atraso"))
                api.requestRenew(loan._id, mapOf("addDays" to 7))
                Toast.makeText(requireContext(), "Solicitação enviada ao administrador.", Toast.LENGTH_SHORT).show()
                load() // recarrega para mostrar "Aguardando..."
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Falha ao solicitar: ${e.message}", Snackbar.LENGTH_LONG).show()
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

            val pendente = l.renewalRequested == true
            chip.text = if (pendente) "AGUARDANDO APROVAÇÃO" else l.status

            btnReturn.visibility = View.GONE
            btnRenew.visibility = View.VISIBLE
            btnRenew.isEnabled = !pendente
            btnRenew.text = if (pendente) "Aguardando..." else "Renovar"
            btnRenew.setOnClickListener { if (!pendente) onRenew(l) }
        }
    }
}
