package com.cipriantxt.carlockapp.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cipriantxt.carlockapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SelectBtDeviceFragment: Fragment() {
    private val requestEnableBt = 1
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var activityPipe: ActivityPipe? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_select_bt_device, container, false)
        activityPipe = requireActivity() as ActivityPipe

        bluetoothManager = requireContext().getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager!!.adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), R.string.bt_list_error, Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
        }

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBt = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBt, requestEnableBt)
        } else {
            printPairedDevice(view)
        }

        val selectBtDeviceRefresh = view.findViewById<FloatingActionButton>(R.id.select_bt_device_refresh)
        selectBtDeviceRefresh.setOnClickListener {
            printPairedDevice(view)
        }

        return view
    }

    @SuppressLint("MissingPermission")
    private fun printPairedDevice(view: View) {
        val pairedDevices: List<BluetoothDevice> = bluetoothAdapter!!.bondedDevices.toList()
        val list: ArrayList<String> = ArrayList()
        pairedDevices.forEach { device ->
            list.add(device.name)
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
        val selectBtDeviceList = view.findViewById<ListView>(R.id.select_bt_device_list)
        selectBtDeviceList.adapter = adapter
        selectBtDeviceList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            activityPipe!!.exchange(pairedDevices[position].address)
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            requireActivity().supportFragmentManager.popBackStack()
        } else {
            printPairedDevice(requireView())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bluetoothManager = null
        bluetoothAdapter = null
        activityPipe = null
    }
}
