
package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.main.HomeFragment
import com.example.myapplication.main.SearchFragment
import com.example.myapplication.main.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            open(HomeFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> open(HomeFragment())
                R.id.nav_search -> open(SearchFragment())
                R.id.nav_profile -> open(ProfileFragment())
            }
            true
        }
    }

    private fun open(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_host, f)
            .commit()
    }
}
