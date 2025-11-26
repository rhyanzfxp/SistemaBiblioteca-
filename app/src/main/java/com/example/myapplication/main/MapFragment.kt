package com.example.myapplication.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.BookRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class MapFragment : Fragment() {

    private lateinit var mapView: ZoomableLibraryMapView
    private lateinit var tvInfo: TextView

    private lateinit var chipGroupMode: ChipGroup
    private lateinit var chipMapa: Chip
    private lateinit var chipLista: Chip

    private lateinit var chipGroupFloor: ChipGroup
    private lateinit var chipTerreo: Chip
    private lateinit var chipSuperior: Chip

    private lateinit var rvShelves: RecyclerView
    private lateinit var shelfAdapter: ShelfListAdapter

    private lateinit var bookRepo: BookRepository

    private lateinit var btnZoomIn: ImageButton
    private lateinit var btnZoomOut: ImageButton

    private var currentFloor: Floor = Floor.GROUND
    private var selectedShelfCode: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_map, container, false)


        val toolbar = v.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            val home = HomeFragment()
            (requireActivity() as MainActivity).open(home)
        }

        bookRepo = BookRepository(requireContext())

        mapView = v.findViewById(R.id.mapView)
        tvInfo = v.findViewById(R.id.tvShelfInfo)

        chipGroupMode = v.findViewById(R.id.chipGroupMode)
        chipMapa = v.findViewById(R.id.chipMapa)
        chipLista = v.findViewById(R.id.chipLista)

        chipGroupFloor = v.findViewById(R.id.chipGroupFloor)
        chipTerreo = v.findViewById(R.id.chipTerreo)
        chipSuperior = v.findViewById(R.id.chipSuperior)

        rvShelves = v.findViewById(R.id.rvShelves)
        rvShelves.layoutManager = LinearLayoutManager(requireContext())

        btnZoomIn = v.findViewById(R.id.btnZoomIn)
        btnZoomOut = v.findViewById(R.id.btnZoomOut)

        setupModeSelector()
        setupFloorSelector()


        btnZoomIn.setOnClickListener {
            mapView.smoothZoom(1.3f)
        }

        btnZoomOut.setOnClickListener {
            mapView.smoothZoom(0.8f)
        }


        val shelfItems = buildShelfItems()
        logShelfDebug(shelfItems)

        shelfAdapter = ShelfListAdapter(shelfItems) { item ->

            selectedShelfCode = item.shelfCode
            currentFloor = item.floor
            applyFloor(currentFloor)

            chipMapa.isChecked = true
            mapView.visibility = View.VISIBLE
            rvShelves.visibility = View.GONE
            btnZoomIn.visibility = View.VISIBLE
            btnZoomOut.visibility = View.VISIBLE

            mapView.highlightShelf(item.shelfCode)
            tvInfo.text =
                "Estante ${item.shelfCode} • Setor: ${item.sector ?: "-"} • Andar: ${if (item.floor == Floor.GROUND) "Térreo" else "Superior"}"
        }
        rvShelves.adapter = shelfAdapter


        val bookId = arguments?.getString("bookId")
        bookId?.let { id ->
            val book = bookRepo.byId(id)
            book?.let {
                selectedShelfCode = it.shelfCode
                currentFloor = inferFloorFromSector(it.sector)
                applyFloor(currentFloor)

                tvInfo.text =
                    "Livro: ${it.title} • Estante: ${it.shelfCode ?: "-"} (${it.sector ?: "-"})"

                it.shelfCode?.let { code ->
                    mapView.highlightShelf(code)
                }
            }
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.post {
            applyFloor(currentFloor)
        }
    }

    private fun setupModeSelector() {
        chipGroupMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipLista -> {
                    mapView.visibility = View.GONE
                    rvShelves.visibility = View.VISIBLE
                    btnZoomIn.visibility = View.GONE
                    btnZoomOut.visibility = View.GONE
                }
                else -> {
                    mapView.visibility = View.VISIBLE
                    rvShelves.visibility = View.GONE
                    btnZoomIn.visibility = View.VISIBLE
                    btnZoomOut.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupFloorSelector() {
        chipGroupFloor.setOnCheckedChangeListener { _, checkedId ->
            currentFloor = when (checkedId) {
                R.id.chipSuperior -> Floor.UPPER
                else -> Floor.GROUND
            }
            applyFloor(currentFloor)
            selectedShelfCode?.let { mapView.highlightShelf(it) }
        }

        chipTerreo.isChecked = (currentFloor == Floor.GROUND)
        chipSuperior.isChecked = (currentFloor == Floor.UPPER)
    }

    private fun applyFloor(floor: Floor) {
        when (floor) {
            Floor.GROUND -> {
                mapView.setMapImage(R.drawable.mapa_biblioteca_terreo)
                mapView.setShelfLocations(getShelfLocations(Floor.GROUND))
                chipTerreo.isChecked = true
            }
            Floor.UPPER -> {
                mapView.setMapImage(R.drawable.mapa_biblioteca_superior)
                mapView.setShelfLocations(getShelfLocations(Floor.UPPER))
                chipSuperior.isChecked = true
            }
        }
    }


    private fun buildShelfItems(): List<ShelfItem> {
        val books = try {
            bookRepo.all()
        } catch (e: Exception) {
            Log.e("MAP_DEBUG", "Erro ao carregar livros: ${e.message}", e)
            emptyList()
        }

        val grouped = books
            .filter { !it.shelfCode.isNullOrBlank() }
            .groupBy { it.shelfCode!!.trim() }

        return grouped.map { (code, list) ->
            val sector = list.firstOrNull()?.sector
            val floor = inferFloorFromSector(sector)
            ShelfItem(
                shelfCode = code,
                sector = sector,
                floor = floor,
                bookCount = list.size
            )
        }.sortedBy { it.shelfCode }
    }


    private fun logShelfDebug(items: List<ShelfItem>) {
        val codes = items.map { it.shelfCode }.toSet()
        val sectors = items.mapNotNull { it.sector }.toSet()
        Log.d("MAP_DEBUG", "ShelfCodes encontrados: $codes")
        Log.d("MAP_DEBUG", "Setores encontrados: $sectors")
    }

    private fun inferFloorFromSector(sector: String?): Floor {
        if (sector == null) return Floor.GROUND
        val s = sector.trim()
        return when (s) {
            "1" -> Floor.UPPER
            else -> Floor.GROUND
        }
    }
}
