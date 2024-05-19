package com.cipriantxt.carlockapp.ui.home

import android.bluetooth.BluetoothDevice

interface ActivityPipe {
    fun exchange(device: BluetoothDevice)
}