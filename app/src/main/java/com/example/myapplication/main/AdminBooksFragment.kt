package com.example.myapplication.main

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
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
import java.io.File

class AdminBooksFragment : Fragment() {

    private lateinit var bookStore: AdminBookStore
    private lateinit var adapter: BooksAdapter

    private var currentCoverPreview: ImageView? = null
    private var selectedCoverPath: String? = null
    private var currentBookId: String? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val path = saveImageToInternalStorage(it)
                selectedCoverPath = path

                currentCoverPreview?.setImageURI(Uri.fromFile(File(path)))
                currentBookId?.let { id -> bookStore.saveLocalCover(id, path) }

                load()
            }
        }

    private fun saveImageToInternalStorage(uri: Uri): String {
        val input = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().filesDir, "cover_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out -> input?.copyTo(out) }
        return file.absolutePath
    }

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
            onClick = { editDialog(it, view) },
            onLongClick = { confirmDelete(it, view) }
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

        val coverPreview = v.findViewById<ImageView>(R.id.imgCoverPreview)
        val pickBtn = v.findViewById<MaterialButton>(R.id.btnPickCover)

        currentCoverPreview = coverPreview
        selectedCoverPath = null
        currentBookId = null

        pickBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

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
                    Snackbar.make(root, "Preencha todos os campos.", Snackbar.LENGTH_LONG).show()
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
                Snackbar.make(root, "Livro adicionado.", Snackbar.LENGTH_LONG).show()
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

        val coverPreview = v.findViewById<ImageView>(R.id.imgCoverPreview)
        val pickBtn = v.findViewById<MaterialButton>(R.id.btnPickCover)

        val localPath = bookStore.localCoverPath(book.id)
        if (localPath != null) {
            coverPreview.setImageURI(Uri.fromFile(File(localPath)))
            selectedCoverPath = localPath
        } else {
            coverPreview.setImageResource(R.drawable.ic_book_placeholder)
            selectedCoverPath = null
        }

        currentCoverPreview = coverPreview
        currentBookId = book.id

        pickBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

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
                    Snackbar.make(root, "Preencha todos os campos.", Snackbar.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                selectedCoverPath?.let { path ->
                    bookStore.saveLocalCover(book.id, path)
                }

                bookStore.update(updated)
                load()
                Snackbar.make(root, "Livro atualizado.", Snackbar.LENGTH_LONG).show()
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
                Snackbar.make(root, "Livro excluído.", Snackbar.LENGTH_LONG).show()
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
