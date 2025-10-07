
package com.example.myapplication.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_UniforLibrary)
        setContentView(R.layout.activity_auth)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_host, LoginFragment())
                .commit()
        }
    }
}
