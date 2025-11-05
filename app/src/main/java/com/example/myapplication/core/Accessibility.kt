package com.example.myapplication.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R
import java.util.Locale

/**
 * Acessibilidade:
 * - Overlays de tema (alto contraste / fonte disléxica) e fontScale
 * - Leitura em voz alta (TTS) com toggle por usuário
 * - Atalhos para LIBRAS via links/app
 */
object Accessibility {

    // ==== NOVO: range unificado de fontScale ====
    const val MIN_FONT_SCALE = 0.50f
    const val MAX_FONT_SCALE = 2.00f
    private fun clampFs(x: Float) = x.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE)

    private fun kHigh(email: String) = "high_${email}"
    private fun kDys(email: String)  = "dys_${email}"
    private fun kFs(email: String)   = "fs_${email}"
    private fun kTts(email: String)  = "tts_${email}"
    private fun kLbs(email: String)  = "libras_${email}"

    data class Prefs(
        val highContrast: Boolean,
        val dyslexicFont: Boolean,
        val fontScale: Float,
        val ttsEnabled: Boolean,
        val librasEnabled: Boolean
    )

    fun read(context: Context, email: String? = null): Prefs {
        val session = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        val mail = email ?: (session.getString("user_email", "") ?: "")
        val p = context.getSharedPreferences("users_prefs", Context.MODE_PRIVATE)
        return Prefs(
            highContrast = p.getBoolean(kHigh(mail), false),
            dyslexicFont = p.getBoolean(kDys(mail), false),
            fontScale    = clampFs(p.getFloat(kFs(mail), 1.0f)),
            ttsEnabled   = p.getBoolean(kTts(mail), false),
            librasEnabled= p.getBoolean(kLbs(mail), false)
        )
    }

    fun write(
        context: Context,
        email: String? = null,
        high: Boolean? = null,
        dys: Boolean?  = null,
        fontScale: Float? = null,
        tts: Boolean? = null,
        libras: Boolean? = null
    ) {
        val session = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        val mail = email ?: (session.getString("user_email", "") ?: "")
        val p = context.getSharedPreferences("users_prefs", Context.MODE_PRIVATE)
        p.edit().apply {
            high?.let   { putBoolean(kHigh(mail), it) }
            dys?.let    { putBoolean(kDys(mail), it) }
            fontScale?.let { putFloat(kFs(mail), clampFs(it)) }
            tts?.let    { putBoolean(kTts(mail), it) }
            libras?.let { putBoolean(kLbs(mail), it) }
        }.apply()
    }

    /** Aplica overlays de tema e fonte na Activity atual. */
    fun applyThemeOverlays(activity: Activity) {
        val prefs = read(activity)

        if (prefs.highContrast) {
            activity.setTheme(R.style.ThemeOverlay_Unifor_HighContrast)
        }
        if (prefs.dyslexicFont) {
            activity.setTheme(R.style.ThemeOverlay_Unifor_Dyslexic)
        }

        // Ajuste de fundo em alto contraste
        if (prefs.highContrast) {
            activity.window.decorView.setBackgroundColor(Color.BLACK)
        }

        // Aplica a fonte disléxica em toda a árvore de views já inflada
        if (prefs.dyslexicFont) {
            val root = activity.findViewById<ViewGroup>(android.R.id.content)
            applyDyslexicFont(root, activity)
        }
    }

    // Percorre a view tree aplicando a fonte
    private fun applyDyslexicFont(view: View?, ctx: Context) {
        if (view == null) return
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyDyslexicFont(view.getChildAt(i), ctx)
            }
        } else if (view is TextView) {
            try {
                val tf = ResourcesCompat.getFont(ctx, R.font.opendyslexic_regular)
                if (tf != null) view.typeface = tf
            } catch (_: Exception) { /* ignora */ }
        }
    }

    // ======== TTS ========
    private var ttsEngine: TextToSpeech? = null

    fun isTtsEnabled(ctx: Context) = read(ctx).ttsEnabled
    fun setTtsEnabled(ctx: Context, enabled: Boolean) = write(ctx, tts = enabled).also {
        if (!enabled) stopTts()
    }

    fun speak(ctx: Context, text: String) {
        if (!isTtsEnabled(ctx) || text.isBlank()) return
        if (ttsEngine == null) {
            ttsEngine = TextToSpeech(ctx.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ttsEngine?.language = Locale("pt", "BR")
                    ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_screen_read")
                }
            }
        } else {
            ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_screen_read")
        }
    }

    fun stopTts() { ttsEngine?.stop() }

    // ======== LIBRAS (atalhos) ========
    fun isLibrasEnabled(ctx: Context) = read(ctx).librasEnabled
    fun setLibrasEnabled(ctx: Context, enabled: Boolean) = write(ctx, libras = enabled)

    fun showLibrasDialog(ctx: Context) {
        val items = arrayOf(
            "Abrir VLibras (portal leve)",
            "Abrir Hand Talk (alternativa)",
            "Instalar app VLibras",
            "Instalar app Hand Talk"
        )

        androidx.appcompat.app.AlertDialog.Builder(ctx)
            .setTitle("Intérprete em LIBRAS")
            .setItems(items) { _, which ->
                val url = when (which) {
                    0 -> "https://www.vlibras.gov.br"
                    1 -> "https://www.handtalk.me/br/apps"
                    2 -> "market://details?id=br.gov.spb.vlibras"
                    else -> "market://details?id=br.com.handtalk.app"
                }
                try {
                    ctx.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    )
                } catch (_: Exception) {
                    val web = when (which) {
                        2 -> "https://play.google.com/store/apps/details?id=br.gov.spb.vlibras"
                        3 -> "https://play.google.com/store/apps/details?id=br.com.handtalk.app"
                        else -> null
                    }
                    if (web != null) {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(web)))
                    }
                }
            }
            .setNegativeButton("Fechar", null)
            .show()
    }

    /** Aplica preferências no root atual do fragment (para fonte disléxica/contraste em views já infladas). */
    fun applyToFragmentView(root: View?, ctx: Context) {
        if (root == null) return
        val prefs = read(ctx)
        if (prefs.dyslexicFont) applyDyslexicFont(root, ctx)
        if (prefs.highContrast) root.setBackgroundColor(Color.BLACK)
    }

    /** Reaplica fontScale/overlays sem perder estado. */
    fun refreshAccessibility(activity: Activity) {
        val prefs = read(activity)
        val cfg = activity.resources.configuration

        // Se o scale mudou, recria a activity (único modo seguro)
        if (cfg.fontScale != prefs.fontScale) {
            val newCfg = android.content.res.Configuration(cfg)
            newCfg.fontScale = prefs.fontScale
            activity.resources.updateConfiguration(newCfg, activity.resources.displayMetrics)
            activity.recreate()
            return
        }

        // Caso contrário, só reaplica os temas visuais
        applyThemeOverlays(activity)
    }

}
