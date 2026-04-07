package com.cia.tcpapp.ui.tcpSecondScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cia.tcpapp.TcpViewModel
import com.github.barteksc.pdfviewer.PDFView

@Composable
fun PdfContent(
    modifier: Modifier = Modifier,
    viewModel: TcpViewModel
) {
    val pdfBytes by viewModel.currentPdfBytes.collectAsState()

    if (pdfBytes == null) {
        // Estado vacío mientras no se haya descargado ningún PDF.
        Box(modifier.fillMaxSize()) {
            Text(
                text = "No PDF available",
                modifier = Modifier.align(Alignment.Center)
            )
        }
        return
    }

    var pdfView: PDFView? = null

    DisposableEffect(Unit) {
        onDispose {
            // Libera la vista nativa para evitar fugas al salir de la pantalla.
            pdfView?.recycle()
            pdfView = null
        }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth(),
        factory = { context ->
            // Inserta un visor Android clásico dentro de Compose.
            PDFView(context, null).also {
                pdfView = it
                it.fromBytes(pdfBytes!!)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .load()
            }
        }
    )
}
