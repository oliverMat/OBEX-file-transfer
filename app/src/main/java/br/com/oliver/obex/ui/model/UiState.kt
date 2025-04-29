package br.com.oliver.obex.ui.model

import android.bluetooth.BluetoothDevice

data class UiState(
    val devices: List<BluetoothDevice>? = null,
    val selectedDevice: BluetoothDevice? = null,
    val selectedFileUri: String? = null
)
