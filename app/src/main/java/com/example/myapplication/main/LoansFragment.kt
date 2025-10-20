package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.Loan
import com.example.myapplication.data.LoanRepository
import com.example.myapplication.data.UserStore
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import java.time.format.DateTimeFormatter

class LoansFragment : Fragment() {

    private lateinit var repo: LoanRepository
    private lateinit var rv: RecyclerView
    private lateinit var empty: LinearLayout
    private lateinit var tg: MaterialButtonToggleGroup

    private enum class Filter { ATIVOS, ATRASADOS, HISTORICO }
    private var currentFilter = Filter.ATIVOS

    private val df = DateTimeFormatter.ofPattern("dd/MM")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_loans, container, false)

        // Toolbar: voltar para a Home
        v.findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            (requireActivity() as com.example.myapplication.MainActivity).open(HomeFragment())
        }

        repo = LoanRepository(requireContext())

        rv = v.findViewById(R.id.rvLoans)
        empty = v.findViewById(R.id.emptyState)
        tg = v.findViewById(R.id.tgFilters)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = LoanAdapter(
            data = emptyList(),
            onRenew = { loan -> onRenew(loan) },
            onReturn = { loan -> onReturn(loan) }
        )

        // Seleciona "Ativos" por padrão e configura mudança de filtro
        val btnAtivos = v.findViewById<MaterialButton>(R.id.btnAtivos)
        tg.check(btnAtivos.id)
        tg.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            currentFilter = when (checkedId) {
                R.id.btnAtivos -> Filter.ATIVOS
                R.id.btnAtrasados -> Filter.ATRASADOS
                else -> Filter.HISTORICO
            }
            load()
        }

        load()
        return v
    }

    /** Carrega a lista do usuário atual; se vazia, semeia exemplos e recarrega. */
    private fun load() {
        val email = UserStore(requireContext())
            .currentUserEmail()
            ?.takeIf { it.isNotBlank() }
            ?: "guest"

        var list = repo.listForUser(email)

        // Se não houver nada para este usuário, cria exemplos automaticamente
        if (list.isEmpty()) {
            repo.seedFor(email)
            list = repo.listForUser(email)
        }

        // Aplica filtro selecionado
        list = when (currentFilter) {
            Filter.ATIVOS -> list.filter { !it.isReturned && !it.isOverdue }
            Filter.ATRASADOS -> list.filter { it.isOverdue }
            Filter.HISTORICO -> list.filter { it.isReturned }
        }

        (rv.adapter as LoanAdapter).submit(list)
        empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun onRenew(loan: Loan) {
        val ok = repo.renew(loan.id)
        if (ok) {
            Toast.makeText(requireContext(), "Empréstimo renovado por +7 dias", Toast.LENGTH_SHORT).show()
            load()
        } else {
            Toast.makeText(requireContext(), "Não foi possível renovar (limite atingido ou devolvido)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onReturn(loan: Loan) {
        val ok = repo.returnBook(loan.id)
        if (ok) {
            Toast.makeText(requireContext(), "Devolvido com sucesso", Toast.LENGTH_SHORT).show()
            load()
        } else {
            Toast.makeText(requireContext(), "Não foi possível devolver", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- Adapter / ViewHolder ----------------

    inner class LoanAdapter(
        private var data: List<Loan>,
        val onRenew: (Loan) -> Unit,
        val onReturn: (Loan) -> Unit
    ) : RecyclerView.Adapter<LoanVH>() {

        fun submit(list: List<Loan>) {
            data = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanVH {
            val v = layoutInflater.inflate(R.layout.item_loan, parent, false)
            return LoanVH(v)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: LoanVH, position: Int) {
            holder.bind(data[position])
        }
    }

    inner class LoanVH(v: View) : RecyclerView.ViewHolder(v) {
        private val img = v.findViewById<android.widget.ImageView>(R.id.imgCover)
        private val tvTitle = v.findViewById<android.widget.TextView>(R.id.tvTitle)
        private val tvDates = v.findViewById<android.widget.TextView>(R.id.tvDates)
        private val chip = v.findViewById<com.google.android.material.chip.Chip>(R.id.chipStatus)
        private val btnRenew = v.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRenew)
        private val btnReturn = v.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReturn)

        fun bind(l: Loan) {
            img.setImageResource(l.coverRes)
            tvTitle.text = l.bookTitle

            val dates = if (l.isReturned) {
                "De ${l.startDate.format(df)} a ${l.dueDate.format(df)} • Devolvido em ${l.returnedDate!!.format(df)}"
            } else {
                "De ${l.startDate.format(df)} a ${l.dueDate.format(df)} • ${l.renewCount} renovações"
            }
            tvDates.text = dates

            when {
                l.isReturned -> { chip.text = "Devolvido"; chip.isCheckable = false }
                l.isOverdue  -> { chip.text = "Atrasado"; chip.isCheckable = false }
                else         -> { chip.text = "Ativo";    chip.isCheckable = false }
            }

            btnRenew.isEnabled  = !l.isReturned
            btnReturn.isEnabled = !l.isReturned

            btnRenew.setOnClickListener { onRenew(l) }
            btnReturn.setOnClickListener { onReturn(l) }
        }
    }
}
