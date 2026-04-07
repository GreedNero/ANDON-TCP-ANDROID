package com.cia.tcpapp

import android.app.Application
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cia.tcpapp.logic.GenericParser
import com.cia.tcpapp.logic.InfoMsg
import com.cia.tcpapp.logic.MaintenanceMsg
import com.cia.tcpapp.logic.Material
import com.cia.tcpapp.logic.MaterialMsg
import com.cia.tcpapp.logic.RejectionLogMsg
import com.cia.tcpapp.logic.RejectionLogs
import com.cia.tcpapp.logic.ResponseLog
import com.cia.tcpapp.logic.ResponseLogsMsg
import com.cia.tcpapp.logic.StopLog
import com.cia.tcpapp.logic.StopLogMsg
import com.cia.tcpapp.net.MessageReceiver
import com.cia.tcpapp.net.TcpClient
import com.cia.tcpapp.ui.tcpLogs.TcpMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@Suppress("DEPRECATION")
class TcpViewModel(application: Application) : AndroidViewModel(application) {
    // Este ViewModel centraliza el estado de UI y coordina conexión, login y mensajes TCP.

    sealed class PostLoginDestination {
        data class SecondScreen(val screenType: String) : PostLoginDestination()
        object MaintenanceScreen : PostLoginDestination()
    }

    private val _activeTest = MutableStateFlow<ScreenType?>(null)
    val activeTest: StateFlow<ScreenType?> = _activeTest

    private val _postLoginDestination =
        MutableStateFlow<PostLoginDestination?>(null)
    val postLoginDestination = _postLoginDestination.asStateFlow()

    enum class ScreenType {
        MANTENIMIENTO,
        MATERIALES,
        CALIDAD,
        PRODUCCION,
        Test5,
        Test6,
        TestFabBuild
    }

    private val nsdManager = application.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var isDiscoveryRunning = false
    private val nsdTag = "NSD"

    private lateinit var messageReceiver: MessageReceiver

    private val _responseLogs = MutableStateFlow<List<ResponseLog>>(emptyList())
    val responseLogs: StateFlow<List<ResponseLog>> = _responseLogs

    private val _stopLogs = MutableStateFlow<List<StopLog>>(emptyList())
    val stopLogs: StateFlow<List<StopLog>> = _stopLogs

    private val _rejectionLogs = MutableStateFlow<List<RejectionLogs>>(emptyList())
    val rejectionLogs: StateFlow<List<RejectionLogs>> = _rejectionLogs

    private val _currentPdfBytes = MutableStateFlow<ByteArray?>(null)
    val currentPdfBytes: StateFlow<ByteArray?> = _currentPdfBytes

    private val _maintenanceOptions = MutableStateFlow(
        listOf(TcpMessage("None"))
    )
    val maintenanceOptions: StateFlow<List<TcpMessage>> = _maintenanceOptions

    private val _materials = MutableStateFlow<List<Material>>(emptyList())
    val materials: StateFlow<List<Material>> = _materials

    private val _selectedMaterial = MutableStateFlow<Material?>(null)

    val selectedMaterial: StateFlow<Material?> = _selectedMaterial

    private val _info = MutableStateFlow(
        listOf(TcpMessage(""))
    )
    val info: StateFlow<List<TcpMessage>> = _info

    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult = _loginResult.asStateFlow()

    sealed class LoginResult {
        object Success : LoginResult()
        data class Error(val reason: String) : LoginResult()
    }

    private val _screenType = MutableStateFlow<ScreenType?>(null)
    val screenType = _screenType.asStateFlow()

    private val _field1 = MutableStateFlow("")
    val field1: StateFlow<String> = _field1.asStateFlow()

    private val _field2 = MutableStateFlow("")
    val field2: StateFlow<String> = _field2.asStateFlow()

    private val tcpClient = TcpClient()
    private var receiveJob: Job? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _messages = MutableStateFlow(
        listOf(TcpMessage(""))
    )

    private var connectionJob: Job? = null


    fun setScreenType(type: ScreenType) {
        _screenType.value = type
    }


    fun startConnectionLoop(host: String, port: Int, msg: String) {
        // Mantiene un intento de conexión activo y vuelve a intentar si el socket cae.
        if (connectionJob?.isActive == true) return
        connectionJob = viewModelScope.launch {
            try {
                while (isActive) {
                    if (!_isConnected.value) {
                        try {
                            withContext(Dispatchers.IO) {
                                tcpClient.connect(host, port)
                            }

                            val appContext = getApplication<Application>()

                            messageReceiver = MessageReceiver(
                                tcpClient = tcpClient,
                                context = appContext,
                                onTextMessage = { handleIncomingMessage(it) },
                                onPing = { tcpClient.send("PONG") },
                                onDisconnected = { handleDisconnection() },
                                onError = { addMessage("Error al recibir mensaje: $it") },
                                onFileReceived = { bytes ->
                                    setPdfBytes(bytes)
                                }
                            )

                            messageReceiver.start(viewModelScope)
                            sendMessage(msg)
                            _isConnected.value = true

                        } catch (e: Exception) {
                            Log.e("TcpViewModel", e.toString())
                            _isConnected.value = false
                        }
                    }
                    delay(3_000)
                }
            } catch (e: Exception) {
                Log.e("TcpViewModel", e.toString())
                _isConnected.value = false
            }
        }
    }

    fun onFieldChange(text: String) {
        _field1.value = text
    }

    fun onFieldChange2(text: String) {
        _field2.value = text
    }

    fun sendData() {
        val f1 = _field1.value
        val f2 = _field2.value

        if (f1.isEmpty() || f2.isEmpty()) return

        // El login se envía con el formato que espera el servidor TCP.
        val payload = "login|${screenType.value}|user=$f1|pass=$f2\n"
        sendMessage(payload)
        _field1.value = ""
        _field2.value = ""
    }

    fun addMessage(text: String) {
        _messages.update { current ->
            (current + TcpMessage(text)).takeLast(10)
        }
    }


    fun handleMaintenance(msg: MaintenanceMsg) {
        // Convierte el JSON de mantenimiento en texto legible para la pantalla.
        val formatted = msg.payload.checks.entries.joinToString("\n\n") { (name, check) ->
            buildString {
                append("• $name\n")
                append("  - state: ${check.state}\n")
                append("  - last checked: ${check.last_checked ?: "N/A"}\n")
                append("  - next check: ${check.next_check ?: "N/A"}")
            }
        }
        val options = msg.payload.checks.keys.toList()

        _maintenanceOptions.value = listOf(
            TcpMessage("None"),
            *options.map { TcpMessage(it) }.toTypedArray()
        )

        _info.update {
            listOf(TcpMessage(formatted))
        }
    }

    fun handleInfo(msg: InfoMsg) {
        addMessage("Info: ${msg.station}")
    }

    fun handleMaterials(msg: MaterialMsg) {
        _materials.value = msg.station.materials
        _selectedMaterial.value = msg.station.materials.firstOrNull()
    }

    fun selectMaterial(material: Material) {
        _selectedMaterial.value = material
    }

    private fun handleIncomingMessage(message: String) {

        // Primero procesa respuestas simples del protocolo y después intenta parsear JSON.
        val parser = GenericParser()

        when {
            message.startsWith("ACK_LOGIN") -> {
                _loginResult.value = LoginResult.Success
                return
            }

            message.startsWith("ERR_LOGIN") -> {
                _loginResult.value = LoginResult.Error(message)
                return
            }
        }

        if (message.startsWith("{")) {
            when (val parsed = parser.parse(message)) {
                is MaintenanceMsg -> handleMaintenance(parsed)
                is InfoMsg -> handleInfo(parsed)
                is MaterialMsg -> handleMaterials(parsed)
                is ResponseLogsMsg -> _responseLogs.update {
                    parsed.data
                }
                is StopLogMsg -> _stopLogs.update {
                    parsed.data
                }
                is RejectionLogMsg -> _rejectionLogs.update {
                    parsed.data
                }
                else -> addMessage("JSON desconocido: $message")
            }
            return
        }

        addMessage(message)
    }


    private fun handleDisconnection() {
        // Evita repetir la misma limpieza si la desconexión ya fue procesada.
        if (!_isConnected.value) return
        _isConnected.value = false
        stopReceivingMessages()
        viewModelScope.launch {
            try {
                tcpClient.disconnect()
            } catch (e: Exception) {
                addMessage("Error al desconectar: ${e.message}")
            }
        }
    }

    fun stopReceivingMessages() {
        receiveJob?.cancel()
        receiveJob = null
    }


    fun sendMessage(message: String) {
        viewModelScope.launch {
            try {
                tcpClient.send(message)
                addMessage("Mensaje enviado: $message")
            } catch (e: Exception) {
                addMessage("Error al enviar mensaje: ${e.message}")
            }
        }
    }

    fun requestLoginFor(destination: PostLoginDestination) {
        _postLoginDestination.value = destination
    }

    fun clearLoginResult() {
        _loginResult.value = null
    }

    fun clearPostLoginDestination() {
        _postLoginDestination.value = null
    }

    fun setPdfBytes(bytes: File) {
        _currentPdfBytes.value = bytes.readBytes()
    }
    fun clearPdfBytes() {
        _currentPdfBytes.value = null
    }

    fun selectTest(test: ScreenType) {
        _activeTest.value = test
    }

    fun clearTest() {
        _activeTest.value = null
    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            // Cuando Android resuelve el servicio descubierto, arranca el bucle TCP.
            Log.d(nsdTag, "Service resolved: name=${serviceInfo.serviceName} host=${serviceInfo.host} port=${serviceInfo.port}")
            val host = serviceInfo.host.hostAddress
            val port = serviceInfo.port

            if (host != null) {
                startConnectionLoop(host, port, "HANDSHAKE|StationTest\n")
            }
        }

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(nsdTag, "Resolve failed: name=${serviceInfo.serviceName} code=$errorCode")
        }
    }


    fun startDiscovery() {
        // Busca servicios publicados en la red local usando NSD/mDNS.

        if (isDiscoveryRunning) return

        discoveryListener = object : NsdManager.DiscoveryListener {

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(nsdTag, "Service found: name=${service.serviceName} type=${service.serviceType}")
                if (service.serviceType == "_ciagateway._tcp.") {
                    Log.d(nsdTag, "Resolving service: ${service.serviceName}")
                    nsdManager.resolveService(service, resolveListener)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.d(nsdTag, "Service lost: name=${service.serviceName} type=${service.serviceType}")
            }

            override fun onDiscoveryStarted(serviceType: String) {
                Log.d(nsdTag, "Discovery started: $serviceType")
                isDiscoveryRunning = true
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(nsdTag, "Discovery stopped: $serviceType")
                isDiscoveryRunning = false
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(nsdTag, "Start discovery failed: type=$serviceType code=$errorCode")
                isDiscoveryRunning = false
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(nsdTag, "Stop discovery failed: type=$serviceType code=$errorCode")
                isDiscoveryRunning = false
            }
        }

        try {
            Log.d(nsdTag, "Starting discovery request")
            nsdManager.discoverServices(
                "_ciagateway._tcp.",
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener
            )
            // Marcamos como activo para permitir stop inmediato si cambia el estado.
            isDiscoveryRunning = true
        } catch (e: IllegalArgumentException) {
            Log.e(nsdTag, "discoverServices failed: ${e.message}", e)
            isDiscoveryRunning = false
        }
    }

    fun stopDiscovery() {
        if (!isDiscoveryRunning) return
        discoveryListener?.let {
            try {
                Log.d(nsdTag, "Stopping discovery request")
                nsdManager.stopServiceDiscovery(it)
            } catch (e: IllegalArgumentException) {
                // Listener no registrado, se ignora para evitar crash.
                Log.e(nsdTag, "stopServiceDiscovery failed: ${e.message}", e)
            } finally {
                isDiscoveryRunning = false
            }
        }
    }

}


