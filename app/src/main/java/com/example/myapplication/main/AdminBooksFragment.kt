package com.example.myapplication.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.AdminBookStore
import com.example.myapplication.data.Book
import com.example.myapplication.net.SessionStore
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class AdminBooksFragment : Fragment() {

    private lateinit var bookStore: AdminBookStore
    private lateinit var adapter: BooksAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_admin_books, container, false)

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

        bookStore = AdminBookStore(requireContext())

        val rv = view.findViewById<RecyclerView>(R.id.rvList)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = BooksAdapter(
            onClick = { editDialog(it, view) },          // editar (tap)
            onLongClick = { confirmDelete(it, view) }    // excluir (long press)
        )
        rv.adapter = adapter
        load()


        view.findViewById<MaterialButton>(R.id.btnAdd).setOnClickListener { addDialog(view) }
        view.findViewById<MaterialButton>(R.id.btnEdit).setOnClickListener {
            Snackbar.make(view, "Toque em um item para editar.", Snackbar.LENGTH_SHORT).show()
        }
        view.findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener {
            Snackbar.make(view, "Pressione e segure um item para excluir.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun load() {
        adapter.submit(bookStore.list())
    }

    private fun addDialog(root: View) {
        val v = layoutInflater.inflate(R.layout.dialog_add_book, null)
        val title   = v.findViewById<TextInputEditText>(R.id.inputTitle)
        val author  = v.findViewById<TextInputEditText>(R.id.inputAuthor)
        val year    = v.findViewById<TextInputEditText>(R.id.inputYear)
        val edition = v.findViewById<TextInputEditText>(R.id.inputEdition)
        val type    = v.findViewById<TextInputEditText>(R.id.inputType)
        val sector  = v.findViewById<TextInputEditText>(R.id.inputSector)

        AlertDialog.Builder(requireContext())
            .setTitle("Adicionar livro")
            .setView(v)
            .setPositiveButton("Salvar") { d, _ ->
                val t  = title.text?.toString()?.trim().orEmpty()
                val a  = author.text?.toString()?.trim().orEmpty()
                val y  = year.text?.toString()?.trim()?.toIntOrNull() ?: 0
                val ed = edition.text?.toString()?.trim().orEmpty()
                val tp = type.text?.toString()?.trim()?.uppercase().orEmpty()
                val sc = sector.text?.toString()?.trim().orEmpty()

                if (t.isBlank() || a.isBlank() || y <= 0 || ed.isBlank() || tp.isBlank() || sc.isBlank()) {
                    Snackbar.make(root, "Preencha todos os metadados obrigatórios.", Snackbar.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                bookStore.add(
                    title = t,
                    author = a,
                    type = tp,
                    year = y,
                    language = "Português",
                    theme = "Geral",
                    edition = ed,
                    synopsis = null,
                    coverRes = R.drawable.ic_book_placeholder,
                    availableCopies = 1,
                    sector = sc,
                    shelfCode = sc
                )
                load()
                Snackbar.make(root, "Salvo em shared_prefs/admin_books.xml", Snackbar.LENGTH_LONG).show()
                d.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun editDialog(book: Book, root: View) {
        val v = layoutInflater.inflate(R.layout.dialog_add_book, null)
        val title   = v.findViewById<TextInputEditText>(R.id.inputTitle);   title.setText(book.title)
        val author  = v.findViewById<TextInputEditText>(R.id.inputAuthor);  author.setText(book.author)
        val year    = v.findViewById<TextInputEditText>(R.id.inputYear);    year.setText(book.year.toString())
        val edition = v.findViewById<TextInputEditText>(R.id.inputEdition); edition.setText(book.edition ?: "")
        val type    = v.findViewById<TextInputEditText>(R.id.inputType);    type.setText(book.type)
        val sector  = v.findViewById<TextInputEditText>(R.id.inputSector);  sector.setText(book.shelfCode ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle("Editar livro")
            .setView(v)
            .setPositiveButton("Salvar") { d, _ ->
                val updated = book.copy(
                    title = title.text?.toString()?.trim().orEmpty(),
                    author = author.text?.toString()?.trim().orEmpty(),
                    year = year.text?.toString()?.trim()?.toIntOrNull() ?: 0,
                    edition = edition.text?.toString()?.trim().orEmpty(),
                    type = type.text?.toString()?.trim()?.uppercase().orEmpty(),
                    sector = sector.text?.toString()?.trim(),
                    shelfCode = sector.text?.toString()?.trim()
                )

                if (updated.title.isBlank() || updated.author.isBlank() || updated.year <= 0 ||
                    (updated.edition ?: "").isBlank() || updated.type.isBlank() || (updated.shelfCode ?: "").isBlank()
                ) {
                    Snackbar.make(root, "Preencha todos os metadados obrigatórios.", Snackbar.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                bookStore.update(updated)
                load()
                Snackbar.make(root, "Livro atualizado em shared_prefs/admin_books.xml", Snackbar.LENGTH_LONG).show()
                d.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(book: Book, root: View) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir livro")
            .setMessage("Deseja excluir “${book.title}”?")
            .setPositiveButton("Excluir") { d, _ ->
                bookStore.delete(book.id)
                load()
                Snackbar.make(root, "Excluído de shared_prefs/admin_books.xml", Snackbar.LENGTH_LONG).show()
                d.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

private class BooksAdapter(
    private val onClick: (Book) -> Unit,
    private val onLongClick: (Book) -> Unit
) : RecyclerView.Adapter<BooksAdapter.VH>() {

    private val items = mutableListOf<Book>()

    fun submit(newItems: List<Book>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvTitle)
        val subtitle: TextView = v.findViewById(R.id.tvSubtitle)
        val right: TextView = v.findViewById(R.id.tvRight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_admin_book_row, parent, false))

    override fun onBindViewHolder(h: VH, position: Int) {
        val b = items[position]
        h.title.text = "${b.title} • ${b.author}"
        h.subtitle.text = "Estante ${b.shelfCode ?: "-"}"
        h.right.text = if ((b.availableCopies) > 0) "Disponível" else "Indisponível"
        h.itemView.setOnClickListener { onClick(b) }
        h.itemView.setOnLongClickListener { onLongClick(b); true }
    }

    override fun getItemCount(): Int = items.size
}
