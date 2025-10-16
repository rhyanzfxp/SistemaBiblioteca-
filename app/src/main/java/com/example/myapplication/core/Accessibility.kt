package com.example.myapplication

import android.app.Activity
import android.content.Context
import com.example.myapplication.R

/**
 * Aplica apenas overlays de tema (alto contraste e dislexia).
 * A escala de fonte (fontScale) deve ser aplicada em MainActivity.attachBaseContext().
 */
object Accessibility {

    private fun kHigh(email: String) = "high_${email}"
    private fun kDys(email: String)  = "dys_${email}"
    private fun kFs(email: String)   = "fs_${email}"

    data class Prefs(
        val highContrast: Boolean,
        val dyslexicFont: Boolean,
        val fontScale: Float
    )

    fun read(context: Context, email: String): Prefs {
        val p = context.getSharedPreferences("users_prefs", Context.MODE_PRIVATE)
        return Prefs(
            highContrast = p.getBoolean(kHigh(email), false),
            dyslexicFont = p.getBoolean(kDys(email), false),
            fontScale    = p.getFloat(kFs(email), 1.0f).coerceIn(0.85f, 1.40f)
        )
    }

    fun write(
        context: Context,
        email: String,
        high: Boolean? = null,
        dys: Boolean?  = null,
        fontScale: Float? = null
    ) {
        val p = context.getSharedPreferences("users_prefs", Context.MODE_PRIVATE)
        p.edit().apply {
            high?.let { putBoolean(kHigh(email), it) }
            dys?.let  { putBoolean(kDys(email), it) }
            fontScale?.let { putFloat(kFs(email), it.coerceIn(0.85f, 1.40f)) }
        }.apply()
    }

    /** Aplica APENAS os overlays no tema da Activity. */
    fun applyThemeOverlays(activity: Activity) {
        val session = activity.getSharedPreferences("session", Context.MODE_PRIVATE)
        val email = session.getString("user_email", "") ?: ""
        val prefs = read(activity, email)

        if (prefs.highContrast) activity.theme.applyStyle(R.style.ThemeOverlay_Unifor_HighContrast, true)
        if (prefs.dyslexicFont) activity.theme.applyStyle(R.style.ThemeOverlay_Unifor_Dyslexic, true)
    }
}
