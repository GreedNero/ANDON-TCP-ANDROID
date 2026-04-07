package com.cia.tcpapp.ui.tcpLogs

// Modelo simple para mensajes mostrados en pantalla con marca temporal.
data class TcpMessage(
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
