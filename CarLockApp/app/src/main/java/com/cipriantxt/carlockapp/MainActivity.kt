package com.cipriantxt.carlockapp

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.cipriantxt.carlockapp.databinding.ActivityMainBinding

class MainActivity: AppCompatActivity(), ActivityPipe {
    private lateinit var binding: ActivityMainBinding
    private var btConnection: ConnectionThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val jobRunningStatus = binding.jobRunningStatus
        setContentView(binding.root)

        jobRunningStatus.hide()
        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
    }

    override fun connectTo(device: BluetoothDevice) {
        if (btConnection != null) {
            btConnection!!.shutdown()
            btConnection = null
        }
        btConnection = ConnectionThread(this@MainActivity, device)
        btConnection!!.start()
    }

    override fun getBtConnection(): ConnectionThread? {
        return btConnection
    }
}
