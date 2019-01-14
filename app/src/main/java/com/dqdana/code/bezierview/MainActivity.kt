package com.dqdana.code.bezierview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bezier.setOnClickListener {
            bezier.toggle()
        }
        bezier_surface.setOnClickListener {
            bezier_surface.toggle()
        }
    }
}
