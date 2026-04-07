package com.cia.tcpapp.logic
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject

// Modelos que representan los distintos mensajes JSON que envía el servidor.
data class ResponseLogsMsg(
    val cmd: String,
    val data: List<ResponseLog>
)

data class StopLogMsg(
    val cmd: String,
    val data: List<StopLog>
)

data class RejectionLogMsg(
    val cmd: String,
    val data: List<RejectionLogs>
)


data class ResponseLog(
    val timestamp: String,
    val test: String,
    val session: String,
    val station: String,
    val responseTime: Double
)

data class StopLog(
    val timestamp: String,
    val test: String,
    val session: String,
    val station: String,
    val stopTime: Double
)

data class RejectionLogs(
    val timestamp: String,
    val test: String,
    val session: String,
    val station: String,
)

data class Material(
    val id: Int,
    val name: String,
    val description: String
)

data class Station(
    val id: Int,
    val name: String,
    val materials: List<Material>
)

data class MaterialMsg(
    val cmd: String,
    val version: Int,
    val station: Station
)


data class MaintenancePayload(
    val checks: Map<String, MaintenanceCheck>
)

data class MaintenanceMsg(
    val cmd: String,
    val version: Int,
    val payload: MaintenancePayload
)

data class MaintenanceCheck(
    val state: String,
    val last_checked: String?,
    val next_check: String?
)

data class InfoMsg(
    val cmd: String,
    val station: String,
    val ip: String,
    val uptime: Int
)

// Parser genérico que decide a qué modelo convertir el JSON según el campo "cmd".
class GenericParser {

    private val gson = Gson()

    fun parse(json: String): Any? {
        return try {
            val base = gson.fromJson(json, JsonObject::class.java)
            when (base.get("cmd")?.asString) {
                "MATERIALS" -> gson.fromJson(json, MaterialMsg::class.java)
                "MAINTENANCE" -> gson.fromJson(json, MaintenanceMsg::class.java)
                "INFO" -> gson.fromJson(json, InfoMsg::class.java)
                "RESPONSE_LOGS" -> gson.fromJson(json, ResponseLogsMsg::class.java)
                "STOP_LOGS" -> gson.fromJson(json, StopLogMsg::class.java)
                "REJECTION_LOGS" -> gson.fromJson(json, RejectionLogMsg::class.java)
                else -> null
            }
        } catch (e: Exception) {
            Log.e("GenericParser", e.toString())
            null
        }
    }

}
