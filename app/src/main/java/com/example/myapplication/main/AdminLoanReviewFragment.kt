package com.example.myapplication.main

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.Http
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class AdminLoanReviewFragment : Fragment() {

    companion object {
        private const val ARG_ID = "loan_id"
        fun newInstance(loanId: String) = AdminLoanReviewFragment().apply {
            arguments = Bundle().apply { putString(ARG_ID, loanId) }
        }
    }

    private val api by lazy { Http.retrofit(requireContext()).create(ApiService::class.java) }
    private val fmt = DateTimeFormatter.ofPattern("dd/MM")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_admin_loan_review, container, false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val loanId = arguments?.getString(ARG_ID)
        if (loanId.isNullOrBlank()) {
            parentFragmentManager.popBackStack()
            return
        }

        view.findViewById<MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        view.findViewById<TextView>(R.id.tvBookTitle).text = "Solicitação #$loanId"
        view.findViewById<TextView>(R.id.tvRequester).text = ""
        view.findViewById<TextView>(R.id.tvDates).text = "Defina a ação para esta solicitação."

        val btnApprove = view.findViewById<View>(R.id.btnApprove)
        val btnReject = view.findViewById<View>(R.id.btnReject)

        fun setLoading(loading: Boolean) {
            btnApprove.isEnabled = !loading
            btnReject.isEnabled = !loading
        }

        btnApprove.setOnClickListener {
            setLoading(true)
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Opção A: ignoramos o retorno (Map<String, Any> no ApiService)
                    api.adminApprove(loanId, mapOf("days" to 7))
                    Snackbar.make(view, "Empréstimo aprovado!", Snackbar.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                } catch (e: Exception) {
                    Snackbar.make(view, "Falha ao aprovar: ${e.message}", Snackbar.LENGTH_LONG).show()
                    setLoading(false)
                }
            }
        }

        btnReject.setOnClickListener {
            setLoading(true)
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    api.adminDeny(loanId, mapOf("reason" to "Indisponível no momento"))
                    Snackbar.make(view, "Empréstimo recusado!", Snackbar.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                } catch (e: Exception) {
                    Snackbar.make(view, "Falha ao recusar: ${e.message}", Snackbar.LENGTH_LONG).show()
                    setLoading(false)
                }
            }
        }
    }
}
