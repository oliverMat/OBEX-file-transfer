package javax.obex

import android.bluetooth.BluetoothSocket
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

class BluetoothObexTransport(private val socket: BluetoothSocket) : ObexTransport {

    fun createInputStream(): InputStream = socket.inputStream

    fun createOutputStream(): OutputStream = socket.outputStream

    override fun getMaxTransmitPacketSize(): Int = 1024

    override fun getMaxReceivePacketSize(): Int = 1024

    override fun isSrmSupported(): Boolean = false

    fun isConnected(): Boolean = socket.isConnected

    override fun create() = TODO("Not yet implemented")

    override fun listen() = throw UnsupportedOperationException("listen() não suportado em cliente")

    override fun close() = socket.close()

    override fun connect() = socket.connect()

    override fun disconnect() = socket.close()

    override fun openInputStream(): InputStream = socket.inputStream

    override fun openOutputStream(): OutputStream = socket.outputStream

    override fun openDataInputStream(): DataInputStream = DataInputStream(socket.inputStream)

    override fun openDataOutputStream(): DataOutputStream = DataOutputStream(socket.outputStream)
}