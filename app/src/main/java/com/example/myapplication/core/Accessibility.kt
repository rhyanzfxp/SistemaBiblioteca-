package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.myapplication.R
import java.util.Locale

/**
 * Acessibilidade:
 * - Overlays de tema (alto contraste / fonte disléxica) e fontScale (já existentes)
 * - Leitura em voz alta (TTS) com toggle por usuário
 * - Atalhos para LIBRAS via VLibras (WebView)
 */
object Accessibility {


    private fun kHigh(email: String) = "high_${email}"
    private fun kDys(email: String)  = "dys_${email}"
    private fun kFs(email: String)   = "fs_${email}"


    private fun kTts(email: String)     = "tts_${email}"
    private fun kLibras(email: String)  = "libras_${email}"

    data class Prefs(
        val highContrast: Boolean,
        val dyslexicFont: Boolean,
        val fontScale: Float,
        // novos
        val ttsEnabled: Boolean,
        val librasEnabled: Boolean
    )

    /** Lê preferências do usuário logado (tira do shared "session"). */
    fun read(context: Context, email: String? = null): Prefs {
        val session = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        val mail = email ?: (session.getString("user_email", "") ?: "")
        val p = context.getSharedPreferences("users_prefs", Context.MODE_PRIVATE)
        return Prefs(
            highContrast = p.getBoolean(kHigh(mail), false),
            dyslexicFont = p.getBoolean(kDys(mail), false),
            fontScale    = p.getFloat(kFs(mail), 1.0f).coerceIn(0.85f, 1.40f),
            ttsEnabled   = p.getBoolean(kTts(mail), false),
            librasEnabled= p.getBoolean(kLibras(mail), false)
        )
    }

    /** Escreve preferências (somente as passadas) do usuário logado. */
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
            high?.let { putBoolean(kHigh(mail), it) }
            dys?.let  { putBoolean(kDys(mail), it) }
            fontScale?.let { putFloat(kFs(mail), it.coerceIn(0.85f, 1.40f)) }
            tts?.let { putBoolean(kTts(mail), it) }
            libras?.let { putBoolean(kLibras(mail), it) }
        }.apply()
    }

    /** Aplica APENAS os overlays no tema da Activity. */
    fun applyThemeOverlays(activity: Activity) {
        val prefs = read(activity)
        if (prefs.highContrast) activity.theme.applyStyle(R.style.ThemeOverlay_Unifor_HighContrast, true)
        if (prefs.dyslexicFont) activity.theme.applyStyle(R.style.ThemeOverlay_Unifor_Dyslexic, true)
        // fontScale você já trata no attachBaseContext da MainActivity
    }


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

    // ======== LIBRAS (VLibras em WebView) ========
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
                    0 -> "https://www.vlibras.gov.br" // portal leve (não /app)
                    1 -> "https://www.handtalk.me/br/apps" // alternativa leve
                    2 -> "market://details?id=br.gov.spb.vlibras" // Play Store (VLibras)
                    else -> "market://details?id=br.com.handtalk.app" // Play Store (Hand Talk)
                }
                try {
                    ctx.startActivity(
                        android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(url)
                        )
                    )
                } catch (_: Exception) {
                    // fallback: abrir via https se market:// não abrir
                    val web = when (which) {
                        2 -> "https://play.google.com/store/apps/details?id=br.gov.spb.vlibras"
                        3 -> "https://play.google.com/store/apps/details?id=br.com.handtalk.app"
                        else -> null
                    }
                    if (web != null) {
                        ctx.startActivity(
                            android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(web)
                            )
                        )
                    }
                }
            }
            .setNegativeButton("Fechar", null)
            .show()
    }

}
