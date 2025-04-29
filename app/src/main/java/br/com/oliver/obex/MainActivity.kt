package br.com.oliver.obex

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import br.com.oliver.obex.ui.model.UiState
import br.com.oliver.obex.ui.theme.OBEXFileTransferTheme
import br.com.oliver.obex.ui.viewmodel.MainViewModel
import br.com.oliver.obex.util.uriToFile

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OBEXFileTransferTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var permissionsGranted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onFileSelected(uri?.uriToFile(context)?.path)
    }

    val permissionsToRequest = remember {
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                add(Manifest.permission.BLUETOOTH)
                add(Manifest.permission.BLUETOOTH_ADMIN)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (permissionsToRequest.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }) {
            permissionsGranted = true
        } else {
            permissionsLauncher.launch(permissionsToRequest)
        }
    }

    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) viewModel.onPermissionGranted()
    }

    MainContent(
        modifier = modifier,
        state = state,
        permissionsGranted = permissionsGranted,
        onSelectDevice = viewModel::selectDevice,
        onFilePick = { launcher.launch("*/*") },
        onSendFile = viewModel::sendFile
    )
}

@SuppressLint("MissingPermission")
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    state: UiState,
    permissionsGranted: Boolean,
    onSelectDevice: (BluetoothDevice) -> Unit,
    onFilePick: () -> Unit,
    onSendFile: () -> Unit
) {
    Column(modifier = modifier.padding(16.dp).fillMaxHeight()) {
        Text("Dispositivos pareados:")
        Spacer(modifier = Modifier.height(8.dp))

        if (!permissionsGranted) {
            Text("Nenhum dispositivo encontrado ou permissão não concedida.")
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.devices.orEmpty()) { device ->
                    val isSelected = device == state.selectedDevice
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectDevice(device) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = device.name.orEmpty(),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onFilePick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Selecionar arquivo")
            }

            Button(
                onClick = onSendFile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainContent() {
    OBEXFileTransferTheme {
        MainContent(
            state = UiState(devices = listOf(), selectedDevice = null),
            permissionsGranted = true,
            onSelectDevice = {},
            onFilePick = {},
            onSendFile = {}
        )
    }
}
