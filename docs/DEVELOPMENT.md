# Desarrollo

## Requisitos
- Android Studio actualizado.
- JDK 11.
- Android SDK con API 31+.

## Ejecutar en desarrollo
1. Abrir el proyecto en Android Studio.
2. Sincronizar Gradle.
3. Conectar un dispositivo o emulador (API 31+).
4. Ejecutar la app desde Android Studio.

## Comandos utiles
```bash
./gradlew assembleDebug
./gradlew installDebug
./gradlew test
```

## Logs y diagnostico
- Logcat tag: `NSD` para discovery y resolve.
- El socket TCP opera en claro (ver `usesCleartextTraffic=true`).

## Estructura de paquetes (referencia rapida)
- `com.cia.tcpapp` entrada, navegacion y DI simple.
- `com.cia.tcpapp.net` TCP y recepcion de mensajes/archivos.
- `com.cia.tcpapp.logic` parseo y modelos.
- `com.cia.tcpapp.ui` pantallas Compose.
