package br.com.oliver.obex

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.oliver.obex.ui.theme.OBEXFileTransferTheme
import br.com.oliver.obex.ui.viewmodel.MainViewModel
import br.com.oliver.obex.util.uriToFile

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.onPermissionGranted()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!viewModel.checkBluetoothPermission()) {
                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                viewModel.onPermissionGranted()
            }
        } else {
            viewModel.onPermissionGranted()
        }

        setContent {
            OBEXFileTransferTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UI(modifier = Modifier.padding(innerPadding),
                        viewModel
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun UI(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onFileSelected( uri!!.uriToFile(context)?.path)
    }


    Column(modifier = Modifier.padding(16.dp).fillMaxHeight()) {
        Text("Dispositivos pareados:")

        Spacer(modifier = Modifier.height(8.dp))

        if (state.devices!!.isEmpty()) {
            Text("Nenhum dispositivo encontrado ou permissão não concedida.")
        } else {
            LazyColumn (modifier = Modifier.weight(weight = 1f)) {
                items(state.devices!!) { device ->
                    val isSelected = device == state.selectedDevice
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.selectDevice(device) },
                    ) {
                        Text(
                            text = device.name?: "",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            Button(modifier = modifier.fillMaxWidth(),onClick = {
                launcher.launch("*/*")
            }) {
                Text("Selecionar arquivo")
            }
            Button(modifier = modifier.fillMaxWidth(), onClick = {
                viewModel.sendFile()
            }) {
                Text("Enviar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OBEXFileTransferTheme {
        UI(Modifier, viewModel())
    }
}