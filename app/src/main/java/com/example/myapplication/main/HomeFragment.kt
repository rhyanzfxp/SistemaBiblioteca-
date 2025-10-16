package com.example.myapplication.main

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough

class HomeFragment : Fragment() {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

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

        // Garante branco no header (sobrepõe overlays de alto contraste)
        b.tvAppName.setTextColor(ContextCompat.getColor(requireContext(), R.color.lib_white))
        b.tvWelcome.setTextColor(ContextCompat.getColor(requireContext(), R.color.lib_white))

        setupChips()
        setupCarousels()

        // Busca → vai para a tela de Buscar
        b.inputSearch.setOnEditorActionListener { _, _, _ ->
            openFragment(SearchFragment())
            true
        }

        // FAB → Chatbot
        b.fabChatbot.setOnClickListener { openFragment(ChatbotFragment()) }

        // Sino topo → Notificações (RF09/RF09.1)
        b.btnTopAction.setOnClickListener { openFragment(NotificationsFragment()) }

        // Avatar topo → Perfil (RF10/RF10.1)
        b.imgAvatar.setOnClickListener { openFragment(ProfileFragment()) }

        // Atualiza o badge na primeira abertura
        updateBellBadge()
    }

    override fun onResume() {
        super.onResume()
        // Atualiza badge quando volta da tela de Notificações
        updateBellBadge()
    }

    /** Mostra/oculta o dot vermelho no sino quando houver notificações não lidas */
    private fun updateBellBadge() {
        val dot = view?.findViewById<View>(R.id.badgeBell)
        if (dot != null) {
            val hasUnread = try {
                com.example.myapplication.data.NotificationStore(requireContext())
                    .unread()
                    .isNotEmpty()
            } catch (_: Exception) { false }
            dot.visibility = if (hasUnread) View.VISIBLE else View.GONE
        }
    }

    /** Abre um fragment substituindo o container principal e empilha no back stack */
    private fun openFragment(f: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, f)
            .addToBackStack(null)
            .commit()
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
            openFragment(MapFragment())
        }
        configChip(R.id.chipFavoritos,   R.drawable.ic_favorite,      "Favoritos") {
            openFragment(FavoritesFragment())
        }
        configChip(R.id.chipEmprestimos, R.drawable.ic_library_books, "Empréstimos") {
            Snackbar.make(b.root, "Empréstimos em breve", Snackbar.LENGTH_SHORT).show()
        }
        configChip(R.id.chipRenovar,     R.drawable.ic_update,        "Renovar") {
            Snackbar.make(b.root, "Renovar em breve", Snackbar.LENGTH_SHORT).show()
        }
        // Chip de Notificações abre a tela
        configChip(R.id.chipNotificacoes, R.drawable.ic_notifications, "Notificações") {
            openFragment(NotificationsFragment())
        }
        configChip(R.id.chipChatbot,      R.drawable.ic_chat,         "Chatbot") {
            openFragment(ChatbotFragment())
        }
        configChip(R.id.chipCategorias,   R.drawable.ic_category,     "Categorias") {
            Snackbar.make(b.root, "Categorias em breve", Snackbar.LENGTH_SHORT).show()
        }
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

    class HSpace(private val spacePx: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: android.graphics.Rect, v: View, p: RecyclerView, s: RecyclerView.State) {
            val pos = p.getChildAdapterPosition(v)
            outRect.right = spacePx
            if (pos == 0) outRect.left = spacePx
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}

data class BookCard(val title: String, val subtitle: String)

class BookCardAdapter(private val items: List<BookCard>) :
    RecyclerView.Adapter<BankCardVH>() {

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): BankCardVH =
        BankCardVH(LayoutInflater.from(p.context).inflate(R.layout.item_book_horizontal, p, false))

    override fun onBindViewHolder(h: BankCardVH, i: Int) = h.bind(items[i])

    override fun getItemCount() = items.size
}

class BankCardVH(view: View) : RecyclerView.ViewHolder(view) {
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
