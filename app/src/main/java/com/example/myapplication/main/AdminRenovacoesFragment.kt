package com.example.myapplication.admin

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class AdminRenovacoesFragment : Fragment() {

    private val api by lazy { Http.retrofit(requireContext()).create(ApiService::class.java) }
    private lateinit var rv: RecyclerView
    private lateinit var empty: LinearLayout

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val v = i.inflate(R.layout.fragment_loans, c, false)

        v.findViewById<MaterialToolbar>(R.id.toolbar).apply {
            title = "Renovações pendentes"
            setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        }
        // Sem filtros nessa tela
        v.findViewById<View>(R.id.tgFilters)?.visibility = View.GONE

        rv = v.findViewById(R.id.rvLoans)
        empty = v.findViewById(R.id.emptyState)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = LoanAdapter(emptyList(),
            onApprove = { l -> approve(l) },
            onDeny    = { l -> deny(l) }
        )

        load()
        return v
    }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val pendentes = api.adminListRenewRequests() // só renewalRequested=true
                (rv.adapter as LoanAdapter).submit(pendentes)
                empty.visibility = if (pendentes.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                showHttpError(e, "Erro ao carregar")
            }
        }
    }

    /** Lê a mensagem JSON { error: "..."} do backend e mostra no Snackbar */
    private fun showHttpError(e: Exception, fallback: String) {
        val msg = when (e) {
            is HttpException -> {
                val raw = e.response()?.errorBody()?.string()
                if (!raw.isNullOrBlank()) {
                    try {
                        val j = JSONObject(raw)
                        j.optString("error").ifBlank { fallback }
                    } catch (_: Exception) {
                        fallback
                    }
                } else fallback
            }
            else -> e.message ?: fallback
        }
        Snackbar.make(requireView(), msg ?: fallback, Snackbar.LENGTH_LONG).show()
    }

    private fun approve(loan: LoanDto) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // usa a quantidade pedida pelo usuário; se não veio, padrão 7
                val days = loan.renewalAddDays ?: 7
                api.adminApproveRenew(loan._id, mapOf("days" to days))
                Toast.makeText(requireContext(), "Renovação aprovada (+$days dias).", Toast.LENGTH_SHORT).show()
                load()
            } catch (e: Exception) {
                showHttpError(e, "Falha ao aprovar")
            }
        }
    }

    private fun deny(loan: LoanDto) {
        val ctx = requireContext()
        val input = android.widget.EditText(ctx).apply { hint = "Motivo (opcional)" }

        MaterialAlertDialogBuilder(ctx)
            .setTitle("Negar renovação")
            .setView(input)
            .setPositiveButton("Negar") { _, _ ->
                val reason = input.text?.toString()?.takeIf { it.isNotBlank() } ?: "Solicitação não aprovada"
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        api.adminDenyRenew(loan._id, mapOf("reason" to reason))
                        Toast.makeText(requireContext(), "Renovação negada.", Toast.LENGTH_SHORT).show()
                        load()
                    } catch (e: Exception) {
                        showHttpError(e, "Falha ao negar")
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ---------------- Adapter / ViewHolder ----------------

    inner class LoanAdapter(
        private var data: List<LoanDto>,
        private val onApprove: (LoanDto) -> Unit,
        private val onDeny: (LoanDto) -> Unit
    ) : RecyclerView.Adapter<LoanVH>() {

        fun submit(list: List<LoanDto>) { data = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(p: ViewGroup, vt: Int): LoanVH {
            val v = layoutInflater.inflate(R.layout.item_loan, p, false)
            return LoanVH(v)
        }

        override fun onBindViewHolder(h: LoanVH, pos: Int) = h.bind(data[pos], onApprove, onDeny)
        override fun getItemCount() = data.size
    }

    inner class LoanVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
        private val tvDates = v.findViewById<TextView>(R.id.tvDates)
        private val chip = v.findViewById<Chip>(R.id.chipStatus)
        private val btnApprove = v.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRenew)
        private val btnDeny = v.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReturn)

        fun bind(l: LoanDto, onApprove: (LoanDto) -> Unit, onDeny: (LoanDto) -> Unit) {
            tvTitle.text = l.bookId?.title ?: "Livro"
            tvDates.text = "De ${l.startDate?.take(10) ?: "--/--"} a ${l.dueDate?.take(10) ?: "--/--"}"

            val renews = l.renewCount ?: 0
            val addDays = l.renewalAddDays ?: 7
            chip.text = "RENOVAÇÃO PENDENTE • ${renews}x • +${addDays}d"

            // Reaproveita os botões do item:
            btnApprove.visibility = View.VISIBLE
            btnDeny.visibility = View.VISIBLE

            btnApprove.text = "Aprovar"
            btnDeny.text = "Negar"

            // Regras client-side para evitar 400:
            val ativo = l.status == "APROVADO" || l.status == "RENOVADO"
            val atingiuLimite = renews >= 1 // seu backend usa MAX_RENEWS = 1
            btnApprove.isEnabled = ativo && !atingiuLimite

            btnApprove.setOnClickListener { onApprove(l) }
            btnDeny.setOnClickListener { onDeny(l) }
        }
    }
}
