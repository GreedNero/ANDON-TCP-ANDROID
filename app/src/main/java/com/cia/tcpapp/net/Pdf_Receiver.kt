package com.cia.tcpapp.net

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.Socket

// Recibe un archivo binario desde el socket usando el tamaño indicado en el header.
suspend fun receiveFile(
    socket: Socket,
    header: String,
    context: Context
): File? {
    return withContext(Dispatchers.IO) {

        val headerParts = header.split("|")
        if (headerParts.size != 3 || headerParts[0] != "FILE") {
            return@withContext null
        }

        val fileSize = headerParts[2].toLong()
        val input = socket.getInputStream()

        val file = File(
            context.cacheDir,
            "pdf_${System.currentTimeMillis()}.pdf"
        )

        // Se guarda temporalmente en caché para que la UI pueda mostrarlo después.
        file.outputStream().use { output ->
            var remaining = fileSize
            val buffer = ByteArray(4096)

            while (remaining > 0) {
                val read = input.read(
                    buffer,
                    0,
                    minOf(buffer.size.toLong(), remaining).toInt()
                )

                if (read == -1) break

                output.write(buffer, 0, read)
                remaining -= read
            }

            if (remaining != 0L) {
                file.delete()
                return@withContext null
            }
        }

        file
    }
}
