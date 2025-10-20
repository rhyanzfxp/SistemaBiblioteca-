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
import java.time.format.DateTimeFormatter

class RenovarFragment : Fragment() {

    private lateinit var repo: LoanRepository
    private lateinit var rv: RecyclerView
    private lateinit var empty: LinearLayout
    private val df = DateTimeFormatter.ofPattern("dd/MM")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_loans, container, false)

        // toolbar com voltar para Home
        v.findViewById<MaterialToolbar>(R.id.toolbar).apply {
            title = "Renovar"
            setNavigationOnClickListener {
                (requireActivity() as com.example.myapplication.MainActivity).open(HomeFragment())
            }
        }

        // esconde os botões de filtro (Ativos/Atrasados/Histórico)
        v.findViewById<View>(R.id.tgFilters)?.visibility = View.GONE

        repo = LoanRepository(requireContext())

        rv = v.findViewById(R.id.rvLoans)
        empty = v.findViewById(R.id.emptyState)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = LoanAdapter(emptyList()) { loan -> onRenew(loan) }

        load()
        return v
    }

    private fun load() {
        val email = UserStore(requireContext()).currentUserEmail()?.takeIf { it.isNotBlank() } ?: "guest"

        var list = repo.listForUser(email)

        // se estiver vazio, cria exemplos
        if (list.isEmpty()) {
            repo.seedFor(email)
            list = repo.listForUser(email)
        }

        // mostra apenas os atrasados e não devolvidos
        list = list.filter { it.isOverdue && !it.isReturned }

        (rv.adapter as LoanAdapter).submit(list)
        empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun onRenew(loan: Loan) {
        val ok = repo.renew(loan.id)
        if (ok) {
            Toast.makeText(requireContext(), "Livro renovado por +7 dias", Toast.LENGTH_SHORT).show()
            load()
        } else {
            Toast.makeText(requireContext(), "Não foi possível renovar (limite atingido ou devolvido)", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- Adapter ----------------

    inner class LoanAdapter(
        private var data: List<Loan>,
        private val onRenew: (Loan) -> Unit
    ) : RecyclerView.Adapter<LoanVH>() {

        fun submit(list: List<Loan>) { data = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanVH {
            val v = layoutInflater.inflate(R.layout.item_loan, parent, false)
            return LoanVH(v)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: LoanVH, position: Int) = holder.bind(data[position])
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
            tvDates.text = "De ${l.startDate.format(df)} a ${l.dueDate.format(df)} • ${l.renewCount} renovações"

            chip.text = "Atrasado"
            chip.isCheckable = false

            btnRenew.visibility = View.VISIBLE
            btnReturn.visibility = View.GONE

            btnRenew.setOnClickListener { onRenew(l) }
        }
    }
}
