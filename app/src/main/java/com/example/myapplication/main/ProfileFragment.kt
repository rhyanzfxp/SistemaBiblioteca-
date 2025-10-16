package com.example.myapplication.main

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.data.User
import com.example.myapplication.data.UserStore
import java.net.URL
import kotlin.concurrent.thread
import com.example.myapplication.Accessibility

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_profile, container, false)

        val img = v.findViewById<ImageView>(R.id.imgAvatar)
        val etName = v.findViewById<EditText>(R.id.etName)
        val etEmail = v.findViewById<EditText>(R.id.etEmail)
        val etAvatarUrl = v.findViewById<EditText>(R.id.etAvatarUrl)
        val cbHigh = v.findViewById<CheckBox>(R.id.cbHighContrast)
        val cbDys = v.findViewById<CheckBox>(R.id.cbDyslexicFont)
        val sbFont = v.findViewById<SeekBar>(R.id.sbFontScale)
        val tvFont = v.findViewById<TextView>(R.id.tvFontScale)
        val btn = v.findViewById<Button>(R.id.btnSave)

        val store = UserStore(requireContext())
        val u = store.currentUser() ?: User("", "", "")
        etName.setText(u.name)
        etEmail.setText(u.email)

        val prefs = requireContext().getSharedPreferences("users_prefs", 0)
        cbHigh.isChecked = prefs.getBoolean("high_${u.email}", false)
        cbDys.isChecked = prefs.getBoolean("dys_${u.email}", false)
        val fs = prefs.getFloat("fs_${u.email}", 1.0f)
        sbFont.progress = (fs * 100).toInt()
        tvFont.text = "Tamanho da fonte: %,.2fx".format(fs)

        fun loadAvatar(url: String) {
            if (url.isBlank()) return
            thread {
                try {
                    URL(url).openStream().use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        requireActivity().runOnUiThread { img.setImageBitmap(bmp) }
                    }
                } catch (_: Exception) {
                    // Silencia erros de rede/formato; você pode mostrar um Toast se quiser
                }
            }
        }

        etAvatarUrl.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) loadAvatar(etAvatarUrl.text.toString())
        }

        sbFont.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val scale = progress / 100f
                tvFont.text = "Tamanho da fonte: %,.2fx".format(scale)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btn.setOnClickListener {
            // Atualiza nome no UserStore
            if (u.email.isNotBlank()) {
                store.updateName(u.email, etName.text.toString().trim())
            }

            // Salva prefs de acessibilidade do usuário
            prefs.edit()
                .putBoolean("high_${u.email}", cbHigh.isChecked)
                .putBoolean("dys_${u.email}", cbDys.isChecked)
                .putFloat("fs_${u.email}", (sbFont.progress / 100f))
                .apply()

            // ✅ Aplica tema (overlays) e recria a Activity para refletir a escala de fonte com segurança
            Accessibility.applyThemeOverlays(requireActivity())
            requireActivity().recreate()

            // Atualiza avatar se informou URL
            val url = etAvatarUrl.text.toString().trim()
            loadAvatar(url)

            Toast.makeText(requireContext(), "Perfil atualizado", Toast.LENGTH_SHORT).show()
        }

        return v
    }
}
