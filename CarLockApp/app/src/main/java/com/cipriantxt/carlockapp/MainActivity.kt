package com.cipriantxt.carlockapp

import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.cipriantxt.carlockapp.databinding.ActivityMainBinding
import com.cipriantxt.carlockapp.ui.home.ActivityPipe
import java.util.UUID

class MainActivity: AppCompatActivity(), ActivityPipe {
    private lateinit var binding: ActivityMainBinding

    companion object {
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private lateinit var deviceAddress: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
    }

    override fun exchange(data: String) {
        deviceAddress = data
        Toast.makeText(this, data + " from MainActivity", Toast.LENGTH_SHORT).show()
    }
}