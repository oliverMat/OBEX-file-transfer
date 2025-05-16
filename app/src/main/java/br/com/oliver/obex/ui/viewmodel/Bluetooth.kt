package br.com.oliver.obex.ui.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.Executors
import javax.obex.BluetoothObexTransport
import javax.obex.ClientSession
import javax.obex.HeaderSet
import javax.obex.Operation
import javax.obex.ResponseCodes

class Bluetooth {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var scope: CoroutineScope


    @SuppressLint("MissingPermission")
    fun loadBondedDevices(): List<BluetoothDevice>? {
        return bluetoothAdapter?.bondedDevices?.toList()
    }

    @SuppressLint("MissingPermission")
    fun sendFile(bluetoothDevice: BluetoothDevice?, filePath: String?) {
        initScope()

        scope.launch(highPriorityDispatcher) {
            try {
                val socket: BluetoothSocket = bluetoothDevice!!.createRfcommSocketToServiceRecord(
                    UUID.fromString(
                    UUID_OPP
                ))
                socket.connect()

                /* Prepara a sessão OBEX */
                val obexTransport = BluetoothObexTransport(socket)
                val session = ClientSession(obexTransport)
                val connectReply = session.connect(null)

                if (connectReply.responseCode != ResponseCodes.OBEX_HTTP_OK) {
                    Log.e("BluetoothOPP", "Erro ao conectar OBEX")
                    return@launch
                }

                val file = File(filePath!!)

                /* Prepara o cabeçalho do arquivo (metadata) */
                val headers = HeaderSet()
                headers.setHeader(HeaderSet.NAME, file.name)
                headers.setHeader(HeaderSet.TYPE, HEADER_TYPE)
                headers.setHeader(HeaderSet.LENGTH, file.length())

                /* Faz o envio do arquivo */
                val op: Operation = session.put(headers)
                val os: OutputStream = op.openOutputStream()
                val fis = FileInputStream(file)

                val buffer = ByteArray(1024)
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }

                fis.close()
                os.close()
                op.close()

                session.disconnect(null)
                session.close()
                socket.close()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    cancelScope()
                }
            }
        }
    }

    private val highPriorityDispatcher = Executors.newFixedThreadPool(1) { runnable ->
        Thread(runnable, THREAD_PRIORITY).apply { priority = Thread.MAX_PRIORITY }
    }.asCoroutineDispatcher()

    private fun initScope() {
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    private fun cancelScope() {
        scope.cancel()
    }

    companion object {
        private const val UUID_OPP = "00001105-0000-1000-8000-00805f9b34fb"
        private const val HEADER_TYPE = "application/octet-stream"
        private const val THREAD_PRIORITY = "HighPriorityThread"
    }
}