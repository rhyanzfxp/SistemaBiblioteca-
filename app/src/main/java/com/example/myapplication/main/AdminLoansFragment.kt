package com.example.myapplication.main

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.LoanRepository
import com.example.myapplication.net.LoanDto
import com.example.myapplication.net.SessionStore
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
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

        val role = (SessionStore(requireContext()).role() ?: "").trim().lowercase()
        if (role != "admin") {
            Snackbar.make(view, "Acesso permitido somente para Administrador.", Snackbar.LENGTH_LONG).show()
            parentFragmentManager.popBackStack(); return
        }

        repo = LoanRepository(requireContext())

        val rv = view.findViewById<RecyclerView>(R.id.rvList)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = LoansAdapter { loan ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_host, AdminLoanReviewFragment.newInstance(loan._id))
                .addToBackStack(null)
                .commit()
        }
        rv.adapter = adapter
        load()
    }

    private fun parse(d: String?): LocalDate? =
        try { if (d.isNullOrBlank()) null else LocalDate.parse(d.substring(0,10)) } catch (_: Exception){ null }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun load() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            try {
                val list = repo.adminList()
                adapter.submit(list)
            } catch (e: Exception) {
                view?.let { Snackbar.make(it, "Erro ao listar: ${e.message}", Snackbar.LENGTH_LONG).show() }
            }
        }
    }

    private inner class LoansAdapter(
        private val onReview: (LoanDto) -> Unit
    ) : RecyclerView.Adapter<LoansAdapter.VH>() {

        private val data = mutableListOf<LoanDto>()

        fun submit(newData: List<LoanDto>) {
            data.clear(); data.addAll(newData); notifyDataSetChanged()
        }

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tvTitle)
            val subtitle: TextView = v.findViewById(R.id.tvSubtitle)
            val btnReview: Button = v.findViewById(R.id.btnReview)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onBindViewHolder(h: VH, position: Int) {
            val e = data[position]
            val bookTitle = when (val b = e.bookId) {
                is Map<*,*> -> (b["title"] as? String) ?: "(Livro)"
                else -> "(Livro)"
            }
            h.title.text = e.bookId?.title ?: "(Livro)"
            val due = parse(e.dueDate)?.format(fmt)
            val req = parse(e.requestedAt)?.format(fmt)

            val who = e.userId?.name ?: e.userId?.email ?: "Usuário"
            h.subtitle.text = when {
                e.status == "PENDENTE" || e.status == "SOLICITADO" -> "Solicitado por $who em ${parse(e.requestedAt)?.format(fmt)}"
                e.returnedAt != null -> "Devolvido em ${parse(e.returnedAt)?.format(fmt)}"
                else -> "Vence: ${parse(e.dueDate)?.format(fmt)}"
            }

            h.subtitle.text = who
            h.btnReview.setOnClickListener { onReview(e) }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_admin_loan_request, parent, false))

        override fun getItemCount(): Int = data.size
    }
}
