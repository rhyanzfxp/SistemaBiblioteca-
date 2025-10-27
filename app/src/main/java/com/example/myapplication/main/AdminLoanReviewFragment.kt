package com.example.myapplication.main

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.data.LoanRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import java.time.format.DateTimeFormatter

class AdminLoanReviewFragment : Fragment() {

    companion object {
        private const val ARG_ID = "loan_id"
        fun newInstance(loanId: String) = AdminLoanReviewFragment().apply {
            arguments = Bundle().apply { putString(ARG_ID, loanId) }
        }
    }

    private val fmt = DateTimeFormatter.ofPattern("dd/MM")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_admin_loan_review, container, false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val loanId = arguments?.getString(ARG_ID) ?: return parentFragmentManager.popBackStack().let { }
        val repo = LoanRepository(requireContext())
        val loan = repo.listAll().firstOrNull { it.id == loanId } ?: run {
            Snackbar.make(view, "Empréstimo não encontrado.", Snackbar.LENGTH_LONG).show()
            parentFragmentManager.popBackStack(); return
        }

        view.findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<TextView>(R.id.tvBookTitle).text = loan.bookTitle
        view.findViewById<TextView>(R.id.tvRequester).text = "Solicitante: ${loan.userEmail}"
        view.findViewById<TextView>(R.id.tvDates).text =
            "Empréstimo: ${loan.startDate.format(fmt)}  •  Devolução: ${loan.dueDate.format(fmt)}"

        view.findViewById<View>(R.id.btnApprove).setOnClickListener {
            val ok = repo.aprovar(loan.id)
            Snackbar.make(view, if (ok) "Empréstimo aprovado!" else "Não foi possível aprovar.", Snackbar.LENGTH_LONG).show()
            if (ok) parentFragmentManager.popBackStack()
        }
        view.findViewById<View>(R.id.btnReject).setOnClickListener {
            val ok = repo.recusar(loan.id)
            Snackbar.make(view, if (ok) "Empréstimo recusado!" else "Não foi possível recusar.", Snackbar.LENGTH_LONG).show()
            if (ok) parentFragmentManager.popBackStack()
        }
    }
}
