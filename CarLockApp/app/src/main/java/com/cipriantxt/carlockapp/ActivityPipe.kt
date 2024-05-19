package com.cipriantxt.carlockapp

import android.bluetooth.BluetoothDevice

interface ActivityPipe {
    fun connectTo(device: BluetoothDevice)
    fun getBtConnection(): ConnectionThread?
}
