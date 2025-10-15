package com.example.myapplication.main

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis

class HomeFragment : Fragment() {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    // --------- Transições suaves (Material) ----------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Saudação com nome da sessão
        val prefs = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
        val displayName = prefs.getString("user_name", null)?.takeIf { it.isNotBlank() } ?: "Usuário"
        b.tvWelcome.text = "Olá, $displayName."

        setupChips()
        setupCarousels()

        // Busca → vai para a tela de Buscar
        b.inputSearch.setOnEditorActionListener { _, _, _ ->
            navigateOrOpen(R.id.nav_search) { SearchFragment() }
            true
        }

        // FAB → Chatbot
        b.fabChatbot.setOnClickListener {
            navigateOrOpen(R.id.nav_chatbot) { ChatbotFragment() }
        }

        // Sininho topo (quando houver destino, habilite)
        b.btnTopAction.setOnClickListener {
            // navigateOrOpen(R.id.nav_notifications) { NotificationsFragment() }
            Snackbar.make(b.root, "Notificações em breve", Snackbar.LENGTH_SHORT).show()
        }
    }

    // --------- Navegação segura (NavController OU FragmentTransaction) ----------
    private fun hasNavController(): Boolean = try {
        findNavController(); true
    } catch (_: Exception) { false }

    private fun navigateOrOpen(destId: Int, fragmentProvider: () -> Fragment) {
        if (hasNavController()) {
            val nav = findNavController()
            val node = nav.graph.findNode(destId)
            if (node != null) {
                try { nav.navigate(destId) } catch (_: Exception) { /* evita double navigate */ }
            } else {
                (requireActivity() as com.example.myapplication.MainActivity).open(fragmentProvider())
            }
        } else {
            (requireActivity() as com.example.myapplication.MainActivity).open(fragmentProvider())
        }
    }

    // --------- Chips com ripple, micro scale e haptic ----------
    private fun setupChips() {
        fun attachTouchFX(v: View) {
            v.setOnTouchListener { view, e ->
                when (e.action) {
                    MotionEvent.ACTION_DOWN ->
                        view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(70).start()
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                        view.animate().scaleX(1f).scaleY(1f).setDuration(90).start()
                }
                false
            }
        }

        fun configChip(id: Int, icon: Int, label: String, onClick: () -> Unit) {
            val v = requireView().findViewById<View>(id)
            v.findViewById<ImageView>(R.id.icon).setImageResource(icon)
            v.findViewById<TextView>(R.id.label).text = label
            attachTouchFX(v)
            v.setOnClickListener {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onClick()
            }
        }

        configChip(R.id.chipMapa,        R.drawable.ic_map_2d,        "Mapa 2D") {
            navigateOrOpen(R.id.nav_map) { MapFragment() }
        }
        configChip(R.id.chipFavoritos,   R.drawable.ic_favorite,      "Favoritos") {
            navigateOrOpen(R.id.nav_favorites) { FavoritesFragment() }
        }
        configChip(R.id.chipEmprestimos, R.drawable.ic_library_books, "Empréstimos") {
            // navigateOrOpen(R.id.nav_loans) { LoansFragment() }
            Snackbar.make(b.root, "Empréstimos em breve", Snackbar.LENGTH_SHORT).show()
        }
        configChip(R.id.chipRenovar,     R.drawable.ic_update,        "Renovar") {
            // navigateOrOpen(R.id.nav_renew) { RenewFragment() }
            Snackbar.make(b.root, "Renovar em breve", Snackbar.LENGTH_SHORT).show()
        }
        configChip(R.id.chipNotificacoes, R.drawable.ic_notifications, "Notificações") {
            // navigateOrOpen(R.id.nav_notifications) { NotificationsFragment() }
            Snackbar.make(b.root, "Notificações em breve", Snackbar.LENGTH_SHORT).show()
        }
        configChip(R.id.chipChatbot,      R.drawable.ic_chat,         "Chatbot") {
            navigateOrOpen(R.id.nav_chatbot) { ChatbotFragment() }
        }
        configChip(R.id.chipCategorias,   R.drawable.ic_category,     "Categorias") {
            // navigateOrOpen(R.id.nav_categories) { CategoriesFragment() }
            Snackbar.make(b.root, "Categorias em breve", Snackbar.LENGTH_SHORT).show()
        }
        // chipOutros ficou opcional no layout; se usar, adicione aqui.
    }

    // --------- Carrosséis com snap + espaçamento ----------
    private fun setupCarousels() {
        val recommended = listOf(
            BookCard("Clean Code", "R. C. Martin • Estante A1"),
            BookCard("Algoritmos", "Sedgewick • Estante B3"),
            BookCard("Engenharia de Software", "Sommerville • Estante C2")
        )
        val topBorrowed = listOf(
            BookCard("Banco de Dados", "Date • Estante D1"),
            BookCard("Refactoring", "Fowler • Estante A5"),
            BookCard("Estruturas de Dados", "Wirth • Estante B1")
        )

        val space = (12 * resources.displayMetrics.density).toInt()
        if (b.rvRecommended.itemDecorationCount == 0) b.rvRecommended.addItemDecoration(HSpace(space))
        if (b.rvTopBorrowed.itemDecorationCount == 0) b.rvTopBorrowed.addItemDecoration(HSpace(space))

        b.rvRecommended.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = BookCardAdapter(recommended)
            PagerSnapHelper().attachToRecyclerView(this)
        }
        b.rvTopBorrowed.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = BookCardAdapter(topBorrowed)
            PagerSnapHelper().attachToRecyclerView(this)
        }
    }

    // Espaço horizontal entre itens
    class HSpace(private val spacePx: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: android.graphics.Rect, v: View, p: RecyclerView, s: RecyclerView.State) {
            val pos = p.getChildAdapterPosition(v)
            outRect.right = spacePx
            if (pos == 0) outRect.left = spacePx
        }
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}


data class BookCard(val title: String, val subtitle: String)

class BookCardAdapter(private val items: List<BookCard>) :
    RecyclerView.Adapter<BookCardAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.tvTitle)
        private val sub   = view.findViewById<TextView>(R.id.tvSubtitle)
        fun bind(it: BookCard) {
            title.text = it.title
            sub.text   = it.subtitle
            itemView.setOnClickListener {
                itemView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

            }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_book_horizontal, p, false))
    override fun onBindViewHolder(h: VH, i: Int) = h.bind(items[i])
    override fun getItemCount() = items.size
}
