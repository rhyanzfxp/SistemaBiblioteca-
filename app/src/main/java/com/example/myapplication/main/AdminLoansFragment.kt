package com.example.myapplication.main

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.Loan
import com.example.myapplication.data.LoanRepository
import com.example.myapplication.net.SessionStore
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import java.time.format.DateTimeFormatter

class AdminLoansFragment : Fragment() {

    private lateinit var repo: LoanRepository
    private lateinit var adapter: LoansAdapter
    private val fmt = DateTimeFormatter.ofPattern("dd/MM")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_admin_loans, container, false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }


        val session = SessionStore(requireContext())
        val role = (session.role() ?: "").trim().lowercase()
        if (role != "admin") {
            Snackbar.make(view, "Acesso permitido somente para Administrador. (role=$role)", Snackbar.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }

        repo = LoanRepository(requireContext())
        repo.seedFor("aluno@exemplo.com")

        val rv = view.findViewById<RecyclerView>(R.id.rvList)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = LoansAdapter(
            onReview = { loan ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.auth_host, AdminLoanReviewFragment.newInstance(loan.id))
                    .addToBackStack(null)
                    .commit()
            }
        )
        rv.adapter = adapter
        load()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun load() {
        val items = repo.listSolicitados() + repo.listAll().filter {
            it.status == "APROVADO" || it.status == "RENOVADO"
        }
        adapter.submit(items)
    }

    private inner class LoansAdapter(
        private val onReview: (Loan) -> Unit
    ) : RecyclerView.Adapter<LoansAdapter.VH>() {

        private val data = mutableListOf<Loan>()

        fun submit(newData: List<Loan>) {
            data.clear()
            data.addAll(newData)
            notifyDataSetChanged()
        }

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tvTitle)
            val subtitle: TextView = v.findViewById(R.id.tvSubtitle)
            val btnReview: Button = v.findViewById(R.id.btnReview)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onBindViewHolder(h: VH, position: Int) {
            val e = data[position]
            h.title.text = e.bookTitle
            val sub = when {
                e.status == "SOLICITADO" -> "Solicitante: ${e.userEmail} • ${e.startDate.format(fmt)}"
                e.isReturned -> "Devolvido em ${e.returnedDate?.format(fmt)}"
                else -> "Vence: ${e.dueDate.format(fmt)} • ${e.userEmail}"
            }
            h.subtitle.text = sub
            h.btnReview.setOnClickListener { onReview(e) }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_admin_loan_request, parent, false))

        override fun getItemCount(): Int = data.size
    }
}
