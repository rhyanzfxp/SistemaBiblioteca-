package com.example.myapplication.main

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.data.User
import com.example.myapplication.data.UserStore
import com.example.myapplication.core.Accessibility
import com.google.android.material.appbar.MaterialToolbar
import com.example.myapplication.net.Http
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.AccessibilityPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.concurrent.thread

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_profile, container, false)

        // Toolbar back
        val toolbar = v.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Views
        val img = v.findViewById<ImageView>(R.id.imgAvatar)
        val etName = v.findViewById<EditText>(R.id.etName)
        val etEmail = v.findViewById<EditText>(R.id.etEmail)
        val etAvatarUrl = v.findViewById<EditText>(R.id.etAvatarUrl)
        val cbHigh = v.findViewById<CheckBox>(R.id.cbHighContrast)
        val cbDys = v.findViewById<CheckBox>(R.id.cbDyslexicFont)
        val sbFont = v.findViewById<SeekBar>(R.id.sbFontScale)
        val tvFont = v.findViewById<TextView>(R.id.tvFontScale)
        val btn = v.findViewById<Button>(R.id.btnSave)

        // Usuário atual (nome/email)
        val store = UserStore(requireContext())
        val u = store.currentUser() ?: User("", "", "")
        etName.setText(u.name)
        etEmail.setText(u.email)

        // ===== Preferências locais =====
        val local = Accessibility.read(requireContext())
        cbHigh.isChecked = local.highContrast
        cbDys.isChecked = local.dyslexicFont

        // Configuração do SeekBar para 0.50x – 2.00x
        sbFont.max = (Accessibility.MAX_FONT_SCALE * 100).toInt() // 200
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            sbFont.min = (Accessibility.MIN_FONT_SCALE * 100).toInt() // 50
        }

        val fsLocal = local.fontScale
        sbFont.progress = (fsLocal * 100).toInt().coerceIn(
            (Accessibility.MIN_FONT_SCALE * 100).toInt(),
            (Accessibility.MAX_FONT_SCALE * 100).toInt()
        )
        tvFont.text = "Tamanho da fonte: %.2fx".format(sbFont.progress / 100f)

        fun loadAvatar(url: String) {
            if (url.isBlank()) return
            thread {
                try {
                    URL(url).openStream().use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        requireActivity().runOnUiThread { img.setImageBitmap(bmp) }
                    }
                } catch (_: Exception) { }
            }
        }

        etAvatarUrl.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) loadAvatar(etAvatarUrl.text.toString())
        }

        sbFont.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val scale = progress / 100f
                tvFont.text = "Tamanho da fonte: %.2fx".format(scale)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // ===== Retrofit / API =====
        val api = Http.retrofit(requireContext()).create(ApiService::class.java)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val remote = api.getAccessibility()
                withContext(Dispatchers.Main) {
                    cbHigh.isChecked = remote.contrast
                    sbFont.progress = when (remote.fontSize) {
                        "small" -> (Accessibility.MIN_FONT_SCALE * 100).toInt() // 50
                        "large" -> (Accessibility.MAX_FONT_SCALE * 100).toInt() // 200
                        else -> 100
                    }
                    tvFont.text = "Tamanho da fonte: %.2fx".format(sbFont.progress / 100f)
                }
            } catch (_: Exception) { }
        }

        // ===== Salvar =====
        btn.setOnClickListener {
            if (u.email.isNotBlank()) {
                store.updateName(u.email, etName.text.toString().trim())
            }

            val scaleNow = (sbFont.progress / 100f)
                .coerceIn(Accessibility.MIN_FONT_SCALE, Accessibility.MAX_FONT_SCALE)

            Accessibility.write(
                context = requireContext(),
                high = cbHigh.isChecked,
                dys = cbDys.isChecked,
                fontScale = scaleNow
            )

            val fontSize = when {
                scaleNow < 0.9f -> "small"
                scaleNow > 1.1f -> "large"
                else -> "normal"
            }

            val body = AccessibilityPrefs(
                fontSize = fontSize,
                contrast = cbHigh.isChecked,
                voiceAssist = false,
                libras = false
            )

            val url = etAvatarUrl.text.toString().trim()
            loadAvatar(url)

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    api.updateAccessibility(body)
                } catch (_: Exception) { }
                withContext(Dispatchers.Main) {
                    Accessibility.refreshAccessibility(requireActivity())
                    requireActivity().recreate()
                    Toast.makeText(requireContext(), "Preferências atualizadas!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        Accessibility.applyToFragmentView(v, requireContext())
        return v
    }
}
