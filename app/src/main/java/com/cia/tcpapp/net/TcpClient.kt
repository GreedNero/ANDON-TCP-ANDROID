package com.cia.tcpapp.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket

// Encapsula la conexión TCP de bajo nivel: abrir socket, enviar, leer y cerrar.
class TcpClient {

    var socket: Socket? = null
        private set

    private var outputStream: BufferedWriter? = null
    private var inputStream: java.io.InputStream? = null

    suspend fun connect(host: String, port: Int) {
        withContext(Dispatchers.IO) {
            val s = Socket()
            s.connect(InetSocketAddress(host, port), 2_500)

            if (!s.isConnected) {
                throw IOException("Socket no conectado")
            }

            socket = s
            outputStream = BufferedWriter(OutputStreamWriter(s.getOutputStream()))
            inputStream = s.getInputStream()
        }
    }

    suspend fun send(message: String) {
        withContext(Dispatchers.IO) {
            outputStream?.let {
                // Cada mensaje se termina en salto de línea para que el receptor lo lea por líneas.
                it.write(message)
                it.newLine()
                it.flush()
            }
        }
    }

    suspend fun receiveLine(): String? {
        return withContext(Dispatchers.IO) {
            inputStream?.let { readLineFromStream(it) }
        }
    }

    fun getInputStream(): java.io.InputStream? = inputStream

    suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            outputStream?.close()
            inputStream?.close()
            socket?.close()
        }
    }

    private fun readLineFromStream(input: java.io.InputStream): String? {
        val buffer = StringBuilder()

        // Lee byte a byte hasta encontrar '\n' o EOF.
        while (true) {
            val byte = input.read()
            if (byte == -1) return null
            if (byte.toChar() == '\n') break
            buffer.append(byte.toChar())
        }

        return buffer.toString()
    }
}
