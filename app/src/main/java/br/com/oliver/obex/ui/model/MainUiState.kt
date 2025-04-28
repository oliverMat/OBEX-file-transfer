package br.com.oliver.obex.ui.model

import android.bluetooth.BluetoothDevice

data class MainUiState(
    val devices: List<BluetoothDevice>? = null,
    val permissionGranted: Boolean = false,
    val selectedDevice: BluetoothDevice? = null,
    val selectedFileUri: String? = null
)
