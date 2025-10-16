package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.main.HomeFragment
import com.example.myapplication.main.SearchFragment
import com.example.myapplication.main.ChatbotFragment
import com.example.myapplication.main.MapFragment
import com.example.myapplication.main.FavoritesFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // ✅ APLICA A ESCALA DE FONTE AQUI (antes de qualquer acesso a Resources)
    override fun attachBaseContext(newBase: Context) {
        val session = newBase.getSharedPreferences("session", Context.MODE_PRIVATE)
        val email = session.getString("user_email", "") ?: ""

        val prefs = newBase.getSharedPreferences("users_prefs", Context.MODE_PRIVATE)
        val scale = prefs.getFloat("fs_${email}", 1.0f).coerceIn(0.85f, 1.40f)

        val cfg = newBase.resources.configuration
        if (cfg.fontScale == scale) {
            super.attachBaseContext(newBase)
            return
        }

        val newCfg = android.content.res.Configuration(cfg)
        newCfg.fontScale = scale
        val ctx = newBase.createConfigurationContext(newCfg)
        super.attachBaseContext(ctx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ APLICA OVERLAYS DE TEMA AQUI (seguro antes de inflar a UI)
        Accessibility.applyThemeOverlays(this)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            open(HomeFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home      -> open(HomeFragment())
                R.id.nav_map       -> open(MapFragment())
                R.id.nav_search    -> open(SearchFragment())
                R.id.nav_favorites -> open(FavoritesFragment())
                R.id.nav_chat      -> open(ChatbotFragment())
            }
            true
        }
    }

    fun open(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, f)
            .commit()
    }
}
