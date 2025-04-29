package br.com.oliver.obex.ui.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import br.com.oliver.obex.ui.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val bluetooth = Bluetooth()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun onPermissionGranted() {
        _uiState.value = _uiState.value.copy(
            devices = bluetooth.loadBondedDevices()
        )
    }

    fun selectDevice(device: BluetoothDevice) {
        _uiState.value = _uiState.value.copy(selectedDevice = device)
    }

    fun sendFile() {
        bluetooth.sendFile(_uiState.value.selectedDevice, _uiState.value.selectedFileUri)
    }

    fun onFileSelected(uri: String?) {
        _uiState.value = _uiState.value.copy(selectedFileUri = uri)
    }
}