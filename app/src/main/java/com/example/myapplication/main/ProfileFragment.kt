package com.example.myapplication.main

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.myapplication.R
import com.example.myapplication.core.Accessibility
import com.example.myapplication.net.AccessibilityPrefs
import com.example.myapplication.net.ApiConfig
import com.example.myapplication.net.ApiService
import com.example.myapplication.net.Http
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileFragment : Fragment() {

    private var selectedPhotoFile: File? = null
    private var selectedMime: String? = null
    private val session by lazy { requireContext().getSharedPreferences("session", android.content.Context.MODE_PRIVATE) }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { handlePickedImage(it) }
        }

    private fun handlePickedImage(uri: Uri) {
        try {
            val input: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val temp = File.createTempFile("avatar_", ".jpg", requireContext().cacheDir)
            FileOutputStream(temp).use { out -> input?.copyTo(out) }
            selectedPhotoFile = temp
            selectedMime = requireContext().contentResolver.getType(uri) ?: "image/jpeg"
            view?.findViewById<ImageView>(R.id.imgAvatar)?.apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(Uri.fromFile(temp))
            }
        } catch (_: Exception) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_profile, container, false)

        v.findViewById<MaterialToolbar>(R.id.toolbar)?.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val imgAvatar = v.findViewById<ImageView>(R.id.imgAvatar)
        val etName = v.findViewById<EditText>(R.id.etName)
        val etEmail = v.findViewById<EditText>(R.id.etEmail)
        val etAvatarUrl = v.findViewById<EditText>(R.id.etAvatarUrl)
        val cbHigh = v.findViewById<CheckBox>(R.id.cbHighContrast)
        val cbDys = v.findViewById<CheckBox>(R.id.cbDyslexicFont)
        val sbFont = v.findViewById<SeekBar>(R.id.sbFontScale)
        val tvFont = v.findViewById<TextView>(R.id.tvFontScale)
        val btnSave = v.findViewById<Button>(R.id.btnSave)
        val btnLogout = v.findViewById<Button>(R.id.btnLogout)

        imgAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
        imgAvatar.setOnClickListener { pickImageLauncher.launch("image/*") }

        val api = Http.retrofit(requireContext()).create(ApiService::class.java)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val me = api.getMyProfile()
                session.edit()
                    .putString("user_name", me.name)
                    .putString("user_photo_url", me.photoUrl ?: "")
                    .apply()
                withContext(Dispatchers.Main) {
                    etName.setText(me.name)
                    etEmail.setText(me.email)
                    me.photoUrl?.let { url ->
                        val full = ApiConfig.baseUrl(requireContext()) + url
                        imgAvatar.load(full) { crossfade(true) }
                        etAvatarUrl.setText(url)
                    }
                }
            } catch (_: Exception) {}
        }

        val local = Accessibility.read(requireContext())
        cbHigh.isChecked = local.highContrast
        cbDys.isChecked = local.dyslexicFont

        sbFont.max = (Accessibility.MAX_FONT_SCALE * 100).toInt()
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            sbFont.min = (Accessibility.MIN_FONT_SCALE * 100).toInt()
        }

        val fsLocal = local.fontScale
        sbFont.progress = (fsLocal * 100).toInt().coerceIn(
            (Accessibility.MIN_FONT_SCALE * 100).toInt(),
            (Accessibility.MAX_FONT_SCALE * 100).toInt()
        )
        tvFont.text = "Tamanho da fonte: %.2fx".format(sbFont.progress / 100f)

        etAvatarUrl.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val url = etAvatarUrl.text.toString().trim()
                if (url.isNotBlank()) {
                    val full = ApiConfig.baseUrl(requireContext()) + url
                    imgAvatar.load(full) { crossfade(true) }
                }
            }
        }

        sbFont.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvFont.text = "Tamanho da fonte: %.2fx".format(progress / 100f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()

            val scaleNow = (sbFont.progress / 100f)
                .coerceIn(Accessibility.MIN_FONT_SCALE, Accessibility.MAX_FONT_SCALE)
            val fontSize = when {
                scaleNow < 0.9f -> "small"
                scaleNow > 1.1f -> "large"
                else -> "normal"
            }
            val prefsBody = AccessibilityPrefs(
                fontSize = fontSize,
                contrast = cbHigh.isChecked,
                voiceAssist = false,
                libras = false
            )

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    var updated = api.updateMyProfile(
                        com.example.myapplication.net.UpdateUserRequest(
                            name = name, email = email
                        )
                    )

                    selectedPhotoFile?.let { f ->
                        val req = f.asRequestBody((selectedMime ?: "image/jpeg").toMediaTypeOrNull())
                        val part = MultipartBody.Part.createFormData("photo", f.name, req)
                        updated = api.uploadMyPhoto(part)
                        selectedPhotoFile = null
                        selectedMime = null
                    }

                    session.edit()
                        .putString("user_name", updated.name)
                        .putString("user_photo_url", updated.photoUrl ?: "")
                        .apply()

                    withContext(Dispatchers.Main) {
                        updated.photoUrl?.let { url ->
                            val full = ApiConfig.baseUrl(requireContext()) + url
                            imgAvatar.load(full) { crossfade(true) }
                            etAvatarUrl.setText(url)
                        }
                        Toast.makeText(requireContext(), "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                    }

                    Accessibility.write(
                        context = requireContext(),
                        high = cbHigh.isChecked,
                        dys = cbDys.isChecked,
                        fontScale = scaleNow
                    )
                    try { api.updateAccessibility(prefsBody) } catch (_: Exception) { }

                    withContext(Dispatchers.Main) {
                        Accessibility.refreshAccessibility(requireActivity())
                        requireActivity().recreate()
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Falha ao atualizar perfil", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnLogout.setOnClickListener {
            confirmLogout()
        }

        Accessibility.applyToFragmentView(v, requireContext())
        return v
    }
    private fun confirmLogout() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Sair da conta")
            .setMessage("Deseja realmente fazer logout?")
            .setPositiveButton("Sim") { _, _ ->

                // Limpa a sessão igual no admin
                val sessionStore = requireContext()
                    .getSharedPreferences("session", android.content.Context.MODE_PRIVATE)
                sessionStore.edit().clear().apply()

                // Limpa também o SessionStore usado no resto do app
                try {
                    com.example.myapplication.net.SessionStore(requireContext()).clear()
                } catch (_: Exception) {}

                // Volta para o LoginFragment
                parentFragmentManager.popBackStack(
                    null,
                    androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
                )

                parentFragmentManager.beginTransaction()
                    .replace(R.id.auth_host, com.example.myapplication.auth.LoginFragment())
                    .commit()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
