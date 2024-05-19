package com.cipriantxt.carlockapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.cipriantxt.carlockapp.ui.home.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.io.IOException
import java.util.ArrayList
import java.util.Collections.synchronizedList
import java.util.UUID
import kotlin.properties.Delegates

@SuppressLint("MissingPermission")
class ConnectionThread(private val activity: MainActivity, device: BluetoothDevice): Thread() {
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val bluetoothSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        device.createInsecureRfcommSocketToServiceRecord(uuid)
    }
    private var shutdown = false
    private val lock = Object()
    private val jobQueue = synchronizedList(ArrayList<Pair<Fragment, String>>())
    private lateinit var lockState: String

    override fun run() {
        val jobRunningStatus = activity.findViewById<LinearProgressIndicator>(R.id.job_running_status)
        val navBar = activity.findViewById<BottomNavigationView>(R.id.nav_view)
        bluetoothManager = activity.getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager!!.adapter
        bluetoothAdapter?.cancelDiscovery()

        bluetoothSocket?.let { socket ->
            activity.runOnUiThread {
                jobRunningStatus.show()
            }
            try {
                socket.connect()
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.bt_connected_to_remote) + socket.remoteDevice.name,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.bt_connection_error) + socket.remoteDevice.name,
                        Toast.LENGTH_SHORT
                    ).show()
                    shutdown()
                }
            } finally {
                activity.runOnUiThread {
                    jobRunningStatus.hide()
                }
            }
            while (!shutdown) {
                if (jobQueue.isEmpty()) {
                    synchronized(lock) {
                        lock.wait()
                    }
                }
                activity.runOnUiThread {
                    navBar.menu.forEach { button -> button.isEnabled = false }
                    jobRunningStatus.show()
                }
                try {
                    val currentJob = jobQueue.removeAt(0)
                    commandParser(currentJob)
                } catch (e: IOException) {
                    activity.runOnUiThread {
                        Toast.makeText(activity, R.string.bt_socket_error, Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    activity.runOnUiThread {
                        jobRunningStatus.hide()
                        navBar.menu.forEach { button -> button.isEnabled = true }
                    }
                }
            }
        }
    }

    private fun commandParser(job: Pair<Fragment, String>) {
        val fragment = job.first
        val command = job.second
        when {
            command == "sync" -> syncCommand(fragment)
            command == "lock" -> lockCommand(fragment)
            command.startsWith("setInfo") -> setInfoCommand(command)
            command.startsWith("hideInfo") -> hideInfoCommand(command)
        }
    }

    private fun syncCommand(fragment: Fragment) {
        val result = issueCommandToSocket("sync").split("\r\n")
        lockState = result[0]
        val status = result[1]
        if (status == "success") {
            updateHomeLockBtnUi(fragment, lockState)
        } else {
            printUnexpectedError()
        }
    }

    private fun lockCommand(fragment: Fragment) {
        val status = issueCommandToSocket("lock").trim()
        if (status == "success") {
            lockState = if (lockState == "Unlocked") {
                "Locked"
            } else {
                "Unlocked"
            }
            updateHomeLockBtnUi(fragment, lockState)
        } else {
            printUnexpectedError()
        }
    }

    private fun setInfoCommand(command: String) {
        val status = issueCommandToSocket(command).trim()
        if (status == "fail") {
            printUnexpectedError()
        }
    }

    private fun hideInfoCommand(command: String) {
        val status = issueCommandToSocket(command).trim()
        if (status == "success") {
            activity.runOnUiThread {
                Toast.makeText(activity, R.string.contact_saved, Toast.LENGTH_SHORT).show()
            }
        } else {
            printUnexpectedError()
        }
    }

    private fun issueCommandToSocket(command: String): String {
        bluetoothSocket!!.outputStream.write(command.toByteArray())
        val waitStartTime = System.nanoTime()
        while (bluetoothSocket!!.inputStream.available() == 0) {
            // Wait for available input stream but shutdown connection if timeout
            if (System.nanoTime() - waitStartTime >= 5e+9) {
                shutdown()
                throw IOException()
            }
        }
        val bytesReceived = bluetoothSocket!!.inputStream.available()
        return readAllBytes(bytesReceived)
    }

    private fun readAllBytes(bytesReceived: Int): String {
        var result = ""
        var remainingBytes = bytesReceived
        while (remainingBytes > 0) {
            result += bluetoothSocket!!.inputStream.read().toChar()
            remainingBytes--
        }
        return result
    }

    private fun updateHomeLockBtnUi(fragment: Fragment, lockState: String) {
        var lockBtnIcon by Delegates.notNull<Int>()
        var lockBtnTitle by Delegates.notNull<Int>()
        var lockBtnDesc by Delegates.notNull<Int>()
        when (lockState) {
            "Locked" -> {
                lockBtnIcon = R.drawable.ic_locked_24dp
                lockBtnTitle = R.string.locked_title
                lockBtnDesc = R.string.locked_desc
            }

            "Unlocked" -> {
                lockBtnIcon = R.drawable.ic_unlocked_24dp
                lockBtnTitle = R.string.unlocked_title
                lockBtnDesc = R.string.unlocked_desc
            }

            else -> {
                lockBtnIcon = R.drawable.ic_question_mark_24dp
                lockBtnTitle = R.string.unknown_lock_state_title
                lockBtnDesc = R.string.unknown_lock_state_desc
            }
        }
        val homeFragment = fragment as HomeFragment
        homeFragment.setLockBtn(lockBtnIcon, lockBtnTitle, lockBtnDesc)
        activity.runOnUiThread {
            fragment.requireView().findViewById<ImageView>(R.id.lock_btn_icon).setImageResource(lockBtnIcon)
            fragment.requireView().findViewById<TextView>(R.id.lock_btn_title).setText(lockBtnTitle)
            fragment.requireView().findViewById<TextView>(R.id.lock_btn_desc).setText(lockBtnDesc)
        }
    }

    private fun printUnexpectedError() = activity.runOnUiThread {
        Toast.makeText(activity, R.string.bt_command_error, Toast.LENGTH_SHORT).show()
    }

    fun enqueueJob(fragment: Fragment, command: String) = synchronized(lock) {
        if (this.isAlive) {
            jobQueue.add(Pair(fragment, command))
            lock.notifyAll()
        } else {
            lockState = "Unknown"
            shutdown()
            updateHomeLockBtnUi(fragment, lockState)
            throw NullPointerException()
        }
    }

    fun shutdown() {
        shutdown = true
        bluetoothSocket?.close()
    }
}
