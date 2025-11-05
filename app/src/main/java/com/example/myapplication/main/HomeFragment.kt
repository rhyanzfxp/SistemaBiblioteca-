package com.example.myapplication.main

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.core.Accessibility
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.Http
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    private var accessibilityMenuProvider: MenuProvider? = null

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
        // ✅ Aplica acessibilidade (fonte + contraste) assim que a tela é criada
        Accessibility.applyToFragmentView(view, requireContext())

        val prefs = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
        val displayName = prefs.getString("user_name", null)?.takeIf { it.isNotBlank() } ?: "Usuário"
        b.tvWelcome.text = "Olá, $displayName."

        // Header branco
        b.tvAppName.setTextColor(ContextCompat.getColor(requireContext(), R.color.lib_white))
        b.tvWelcome.setTextColor(ContextCompat.getColor(requireContext(), R.color.lib_white))

        setupChips()
        setupCarousels()
        attachAccessibilityMenu()

        // Busca → Buscar
        b.inputSearch.setOnEditorActionListener { _, _, _ ->
            openFragment(SearchFragment()); true
        }

        // FAB → Chatbot
        b.fabChatbot.setOnClickListener { openFragment(ChatbotFragment()) }

        // Sino → Notificações
        b.btnTopAction.setOnClickListener { openFragment(NotificationsFragment()) }

        // Avatar → Perfil
        b.imgAvatar.setOnClickListener { openFragment(ProfileFragment()) }

        // Badge do sino (consulta backend)
        updateBellBadge()

        // === FAB de LIBRAS (flutuante) ===
        addLibrasFab()

        // Se TTS ativo, ler resumo
        if (Accessibility.read(requireContext()).ttsEnabled) {
            Accessibility.speak(
                requireContext(),
                "Home. Use os chips para navegar: Mapa dois D, Favoritos, Empréstimos, Renovar, Notificações e Chatbot. Campo de busca logo abaixo."
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // ✅ Garante reaplicação da acessibilidade ao voltar para a Home
        Accessibility.applyToFragmentView(view, requireContext())
        updateBellBadge()
    }

    override fun onDestroyView() {
        accessibilityMenuProvider?.let { (requireActivity() as MenuHost).removeMenuProvider(it) }
        accessibilityMenuProvider = null
        _b = null
        super.onDestroyView()
    }

    private fun addLibrasFab() {
        val margin = (16 * resources.displayMetrics.density).toInt()
        val fab = com.google.android.material.floatingactionbutton.FloatingActionButton(requireContext()).apply {
            setImageResource(R.drawable.ic_hand_sign_24)
            contentDescription = "Ativar intérprete em Libras"
            compatElevation = 8f
            setOnClickListener { Accessibility.showLibrasDialog(requireContext()) }
            setOnLongClickListener {
                val cur = Accessibility.read(requireContext()).librasEnabled
                Accessibility.write(requireContext(), libras = !cur)
                Snackbar.make(b.root, if (!cur) "LIBRAS ativado" else "LIBRAS desativado", Snackbar.LENGTH_SHORT).show()
                true
            }
        }

        val root = b.root as ViewGroup
        val lpBase = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = margin
            marginStart = margin
        }

        when (root) {
            is androidx.constraintlayout.widget.ConstraintLayout -> {
                val lp = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(lpBase).apply {
                    startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                }
                fab.layoutParams = lp
            }
            is android.widget.FrameLayout -> {
                val lp = android.widget.FrameLayout.LayoutParams(lpBase).apply {
                    gravity = android.view.Gravity.START or android.view.Gravity.BOTTOM
                }
                fab.layoutParams = lp
            }
            else -> {
                fab.layoutParams = lpBase
                fab.post {
                    fab.translationX = margin.toFloat()
                    fab.translationY = (root.height - fab.height - margin).toFloat()
                }
            }
        }

        fab.z = 10f
        root.addView(fab)
    }

    private fun attachAccessibilityMenu() {
        val host: MenuHost = requireActivity()
        val provider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_accessibility, menu)
                val prefs = Accessibility.read(requireContext())
                menu.findItem(R.id.action_toggle_tts)?.isChecked = prefs.ttsEnabled
                menu.findItem(R.id.action_toggle_libras)?.isChecked = prefs.librasEnabled
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.action_toggle_tts -> {
                        val newState = !item.isChecked
                        item.isChecked = newState
                        Accessibility.write(requireContext(), tts = newState)
                        if (newState) Accessibility.speak(requireContext(), "Leitura em voz alta ativada. Esta é a tela inicial.")
                        else Accessibility.stopTts()
                        true
                    }
                    R.id.action_toggle_libras -> {
                        val newState = !item.isChecked
                        item.isChecked = newState
                        Accessibility.write(requireContext(), libras = newState)
                        if (newState) Accessibility.showLibrasDialog(requireContext())
                        true
                    }
                    else -> false
                }
            }
        }
        host.addMenuProvider(provider, viewLifecycleOwner, Lifecycle.State.RESUMED)
        accessibilityMenuProvider = provider
    }

    /** Consulta o backend para saber se há notificações não lidas e mostra o badge. */
    private fun updateBellBadge() {
        val dot = view?.findViewById<View>(R.id.badgeBell) ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val api = Http.retrofit(requireContext()).create(ApiService::class.java)
            val hasUnread = try {
                api.notifications(onlyUnread = true).isNotEmpty()
            } catch (_: Exception) {
                false
            }
            dot.visibility = if (hasUnread) View.VISIBLE else View.GONE
        }
    }

    private fun openFragment(f: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, f)
            .addToBackStack(null)
            .commit()
    }

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

        configChip(R.id.chipMapa, R.drawable.ic_map_2d, "Mapa 2D") {
            openFragment(MapFragment())
        }
        configChip(R.id.chipFavoritos, R.drawable.ic_favorite, "Favoritos") {
            openFragment(FavoritesSelectFragment())
        }
        configChip(R.id.chipEmprestimos, R.drawable.ic_library_books, "Empréstimos") {
            openFragment(LoansFragment())
        }
        configChip(R.id.chipRenovar, R.drawable.ic_update, "Renovar") {
            openFragment(RenovarFragment())
        }
        configChip(R.id.chipNotificacoes, R.drawable.ic_notifications, "Notificações") {
            openFragment(NotificationsFragment())
        }
        configChip(R.id.chipChatbot, R.drawable.ic_chat, "Chatbot") {
            openFragment(ChatbotFragment())
        }
        configChip(R.id.chipCategorias, R.drawable.ic_category, "Categorias") {
            Snackbar.make(b.root, "Categorias em breve", Snackbar.LENGTH_SHORT).show()
        }
    }

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
    private val sub = view.findViewById<TextView>(R.id.tvSubtitle)
    fun bind(it: BookCard) {
        title.text = it.title
        sub.text = it.subtitle
        itemView.setOnClickListener {
            itemView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
}
