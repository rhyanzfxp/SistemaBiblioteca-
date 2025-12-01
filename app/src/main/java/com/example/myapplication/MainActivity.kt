package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.core.Accessibility
import com.example.myapplication.main.HomeFragment
import com.example.myapplication.main.SearchFragment
import com.example.myapplication.main.ChatbotFragment
import com.example.myapplication.main.MapFragment
import com.example.myapplication.main.FavoritesFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun attachBaseContext(newBase: Context) {
        val session = newBase.getSharedPreferences("session", Context.MODE_PRIVATE)
        val email = session.getString("user_email", "") ?: ""
        val prefs = newBase.getSharedPreferences("users_prefs", Context.MODE_PRIVATE)

        val scale = prefs
            .getFloat("fs_${email}", 1.0f)
            .coerceIn(Accessibility.MIN_FONT_SCALE, Accessibility.MAX_FONT_SCALE)

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
        super.onCreate(savedInstanceState)
        Accessibility.applyThemeOverlays(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Accessibility.applyThemeOverlays(this)

        if (savedInstanceState == null) {
            open(HomeFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.nav_home      -> {
                    open(HomeFragment())
                    binding.bottomNav.menu.findItem(R.id.nav_home).isChecked = true
                }
                R.id.nav_map       -> {
                    open(MapFragment())
                    binding.bottomNav.menu.findItem(R.id.nav_map).isChecked = true
                }
                R.id.nav_search    -> {
                    open(SearchFragment())
                    binding.bottomNav.menu.findItem(R.id.nav_search).isChecked = true
                }
                R.id.nav_favorites -> {
                    open(FavoritesFragment())
                    binding.bottomNav.menu.findItem(R.id.nav_favorites).isChecked = true
                }
                R.id.nav_chat      -> {
                    open(ChatbotFragment())
                    binding.bottomNav.menu.findItem(R.id.nav_chat).isChecked = true
                }
            }
            true
        }


        lifecycleScope.launchWhenResumed {
            val prefs = getSharedPreferences("users_prefs", Context.MODE_PRIVATE)
            prefs.registerOnSharedPreferenceChangeListener { _, key ->
                if (key?.startsWith("fs_") == true ||
                    key?.startsWith("dys_") == true ||
                    key?.startsWith("high_") == true
                ) {
                    Accessibility.refreshAccessibility(this@MainActivity)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Accessibility.applyThemeOverlays(this)
    }

    fun open(fragment: Fragment): Boolean {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
        return true
    }
}
