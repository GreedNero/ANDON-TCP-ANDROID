package com.cia.tcpapp.net

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

// Escucha el socket en segundo plano y despacha cada mensaje al callback adecuado.
class MessageReceiver(
    private val tcpClient: TcpClient,
    private val context: Context,
    private val onTextMessage: (String) -> Unit,
    private val onPing: suspend () -> Unit,
    private val onDisconnected: () -> Unit,
    private val onError: (String) -> Unit,
    private val onFileReceived: suspend (File) -> Unit

) {

    fun start(scope: CoroutineScope) {
        scope.launch {
            while (isActive) {
                try {
                    val message = tcpClient.receiveLine()

                    if (message == null) {
                        onDisconnected()
                        break
                    }

                    if (message == "PING") {
                        // Mantiene viva la sesión respondiendo al heartbeat del servidor.
                        onPing()
                        continue
                    }

                    if (message.startsWith("FILE|")) {
                        // Cuando llega un encabezado de archivo, cambia a modo recepción binaria.
                        val socket = tcpClient.socket ?: break
                        val file = receiveFile(socket, message, context)
                        if (file != null) {
                            onFileReceived(file)
                        }
                        continue
                    }

                    onTextMessage(message)

                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    onError(e.message ?: "Unknown error")
                    onDisconnected()
                    break
                }
            }
        }
    }
}
