package br.com.oliver.obex.ui.viewmodel

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.oliver.obex.ui.model.MainUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val bluetooth = Bluetooth()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState


    fun checkBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getApplication<Application>().checkSelfPermission(
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun onPermissionGranted() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                devices = bluetooth.loadBondedDevices(),
                permissionGranted = true
            )
        }
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