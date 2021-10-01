package com.example.ytdownloader

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation : BottomNavigationView = findViewById(R.id.bottom_navigation)

        val sectionText : TextView = findViewById(R.id.current_section)

        sectionText.text = "Sections"

        bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.ic_list -> {
                    sectionText.text = "Sections"
                }

                R.id.ic_artist-> {
                    sectionText.text = "Artists"
                }

                R.id.ic_album-> {
                    sectionText.text = "Albums"
                }

                R.id.ic_song-> {
                    sectionText.text = "Songs"
                }
            }
            true
        }
    }
}