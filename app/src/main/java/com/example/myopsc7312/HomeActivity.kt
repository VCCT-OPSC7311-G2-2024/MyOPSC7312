package com.example.myopsc7312

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            val settingsFragment = Settings()

            // Get the userId passed from LoginActivity
            val userId = intent.getStringExtra("USER_ID")

            // Create a Bundle and put the userId in it
            val bundle = Bundle()
            bundle.putString("USER_ID", userId)

            // Set the arguments for the fragment
            settingsFragment.arguments = bundle

            // Add the fragment to the container
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, settingsFragment)
                .commit()
        }
    }
}