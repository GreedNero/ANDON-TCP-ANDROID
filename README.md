# TCP App

Aplicacion Android (Jetpack Compose) que descubre un servicio TCP via NSD/mDNS, se conecta y consume un protocolo de mensajes para mostrar pantallas operativas (mantenimiento, materiales, calidad, produccion, etc.).

**Documentacion adicional**
- `docs/ARCHITECTURE.md` (arquitectura tecnica y flujo de datos)

## Requisitos
- Android Studio actualizado.
- JDK 11.
- Android SDK con `minSdk 31` o superior.
- Un servicio TCP en la misma red local anunciando `_ciagateway._tcp.`.

## Inicio rapido
1. Abrir el proyecto en Android Studio y sincronizar Gradle.
2. Conectar un dispositivo o emulador (API 31+).
3. Ejecutar la app desde Android Studio.

Comandos utiles:
```bash
./gradlew assembleDebug
./gradlew installDebug
```

## Estructura del proyecto (alto nivel)
- `app/` codigo de la app y UI Compose.
- `docs/` documentacion tecnica.
- `gradle/` y `gradlew*` wrapper de Gradle.

## Flujo general de la app
- **Main screen**: botones para iniciar/detener pruebas y navegar a subpantallas.
- **Login**: se solicita antes de acceder a ciertas pantallas protegidas.
- **Second screen**: detalle por tipo de prueba (MANTENIMIENTO, MATERIALES, CALIDAD, PRODUCCION, Test5, Test6).
- **Maintenance screen**: vista dedicada tras login de mantenimiento.

## Comunicacion y protocolo (resumen)
- Descubrimiento por NSD/mDNS con servicio `_ciagateway._tcp.`.
- Al resolver el servicio se inicia un handshake: `HANDSHAKE|Station Name`.
- Recepcion de archivos: `FILE|<nombre>|<bytes>` seguido del binario.
- JSON con campo `cmd`: `MATERIALS`, `MAINTENANCE`, `INFO`, `RESPONSE_LOGS`, `STOP_LOGS`, `REJECTION_LOGS`.
- Login: `ACK_LOGIN` y `ERR_LOGIN` como respuestas simples.

## Build y tests
```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew test
```

## Notas de configuracion
- La app permite trafico en claro (`usesCleartextTraffic=true`) para el socket TCP.
- La deteccion NSD puede tardar; revisa Logcat con tag `NSD`.
- Asegura que dispositivo y servidor esten en la misma red.

## Publicacion en GitHub
El repositorio incluye un `.gitignore` para Android/Gradle/IDE y excluye `local.properties`, builds y archivos sensibles.

## Licencia
Apache-2.0. Ver `LICENSE`.
