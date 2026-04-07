# Arquitectura tecnica

## Vision general
La app es una interfaz Compose que se conecta a un backend TCP descubierto por NSD/mDNS. El estado principal vive en un `TcpViewModel`, que coordina:
- Descubrimiento de servicio (NSD).
- Conexion TCP y recepcion continua de mensajes.
- Parseo y distribucion de datos hacia la UI.

## Capas y componentes
**UI (Compose)**
- Entrada: `app/src/main/java/com/cia/tcpapp/MainActivity.kt`
- Navegacion: `app/src/main/java/com/cia/tcpapp/AppNavigation.kt`
- Main screen: `app/src/main/java/com/cia/tcpapp/ui/tcpScreen/TcpMainScreen.kt`
- Login: `app/src/main/java/com/cia/tcpapp/ui/tcpLoginScreen/LoginScreen.kt`
- Second screen: `app/src/main/java/com/cia/tcpapp/ui/tcpSecondScreen/SecondScreen.kt`
- Maintenance: `app/src/main/java/com/cia/tcpapp/ui/maintenanceScreen/MaintenanceScreen.kt`
- PDF: `app/src/main/java/com/cia/tcpapp/ui/tcpSecondScreen/PdfTab.kt`

**State / Coordinacion**
- `app/src/main/java/com/cia/tcpapp/TcpViewModel.kt` con `StateFlow` para conexion, logs, materiales y login.
- Conexion TCP con reintentos y limpieza al desconectar.
- NSD discovery + resolve.
- Enrutado de mensajes entrantes.

**Networking**
- TCP socket: `app/src/main/java/com/cia/tcpapp/net/TcpClient.kt`
- Receiver: `app/src/main/java/com/cia/tcpapp/net/MessageReceiver.kt`
- Archivos: `app/src/main/java/com/cia/tcpapp/net/Pdf_Receiver.kt`

**Parsing / Modelos**
- `app/src/main/java/com/cia/tcpapp/logic/parser.kt` con modelos JSON y `GenericParser` por `cmd`.

## Flujo de datos
1. `TcpMainScreen` inicia o detiene pruebas y navega a subpantallas.
2. `TcpViewModel` inicia discovery NSD; al resolver servicio, abre TCP y manda handshake.
3. `MessageReceiver` mantiene un loop de lectura y procesa `PING`, archivos y JSON.
4. Las pantallas observan `StateFlow` y renderizan el estado actual.

## Diagrama de flujo (texto)
```text
[MainActivity]
    |
    v
[ViewContainer/Scaffold]
    |
    v
[AppNavigation] ----> [NavHost]
    |                   |
    |                   +--> TcpMainScreen
    |                   +--> LoginScreen
    |                   +--> SecondScreen
    |                   +--> MaintenanceScreen
    |
    v
[TcpViewModel]
    |
    +--> NSD discovery (_ciagateway._tcp.)
    |       |
    |       v
    |   resolveService -> host/port
    |       |
    |       v
    +--> TcpClient.connect(host, port)
            |
            v
        MessageReceiver loop
            |
            +--> PING -> PONG
            +--> FILE|... -> cache -> currentPdfBytes
            +--> JSON -> GenericParser -> StateFlow updates
            |
            v
        Compose UI observa StateFlow
```

## Navegacion
Se usa `androidx.navigation.compose`. Rutas principales:
- `AppRoutes.TcpMainScreen` (inicio)
- `AppRoutes.LoginScreen`
- `AppRoutes.MaintenanceScreen`
- `SecondScreen/{screenType}` para detalles por tipo de prueba

## Consideraciones
- La red es TCP en claro (ver `usesCleartextTraffic`).
- NSD/mDNS depende de red local y puede ser lento.
- `TcpViewModel` es compartido por todas las pantallas via `AppViewModelFactory`.
