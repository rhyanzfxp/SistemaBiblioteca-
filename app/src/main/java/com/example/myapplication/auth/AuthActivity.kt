package com.example.myapplication.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.data.UserStore // << add

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_UniforLibrary)
        setContentView(R.layout.activity_auth)

        // << garante que existe um admin ativo
        UserStore(this).ensureAdmin()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_host, LoginFragment())
                .commit()
        }
    }
}
