package com.cia.tcpapp.ui.tcpSecondScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cia.tcpapp.TcpViewModel
import com.cia.tcpapp.logic.RejectionLogs
import com.cia.tcpapp.logic.ResponseLog
import com.cia.tcpapp.logic.StopLog

@Composable
fun InfoProduction(modifier: Modifier, viewmodel: TcpViewModel){
    // Panel de producción con métricas generales y pestañas de trazabilidad.
    val items = listOf(
        TabItemProduction(
            title = "Main Tab",
            unselectedIcon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home
        ),
        TabItemProduction(
            title = "Production log",
            unselectedIcon = Icons.Outlined.PrecisionManufacturing,
            selectedIcon = Icons.Filled.PrecisionManufacturing
        ),
        TabItemProduction(
            title = "Rejection log",
            unselectedIcon = Icons.Outlined.ReportProblem,
            selectedIcon = Icons.Filled.ReportProblem
        ),
        TabItemProduction(
            title = "Stop log",
            unselectedIcon =Icons.Outlined.PauseCircle,
            selectedIcon =Icons.Filled.PauseCircle
        )
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTabIndex, Modifier.zIndex(1f).fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(item.title) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTabIndex == index)
                                item.selectedIcon
                            else
                                item.unselectedIcon,
                            contentDescription = item.title
                        )
                    }

                )
            }

        }
        when (selectedTabIndex) {
            0 -> {
                InfoProductionContent(
                    modifier = Modifier.weight(1f),
                    viewmodel = viewmodel
                )
            }
            1 -> {
                InfoProductionLogs(
                    modifier = Modifier.weight(1f),
                    viewmodel = viewmodel
                )
            }
            2 -> {
                InfoRejectionLogs(
                    modifier = Modifier.weight(1f),
                    viewmodel = viewmodel
                )
            }
            3 -> {
                InfoProductionStop(
                    modifier = Modifier.weight(1f),
                    viewmodel = viewmodel
                )
            }
        }
    }
}


@Composable
fun InfoProductionContent(modifier: Modifier, viewmodel: TcpViewModel){
    val responseLogs by viewmodel.responseLogs.collectAsState()
    val stopLogs by viewmodel.stopLogs.collectAsState()
    val rejectLogs by viewmodel.rejectionLogs.collectAsState()

    val productionEvents = responseLogs.size
    val stopEvents = stopLogs.size
    val rejectEvents = rejectLogs.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Resume el volumen de eventos operativos de la sesión actual.

        Text(
            text = "Production Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            MetricCard(
                title = "Production Events",
                value = productionEvents.toString()
            )

            MetricCard(
                title = "Stops",
                value = stopEvents.toString()
            )

            MetricCard(
                title = "Rejects",
                value = rejectEvents.toString()
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            onClick = {
                // Escalado manual para demo de soporte/supervisión.
                viewmodel.sendMessage("CALL_SUPERVISOR")
            }
        ) {
            Text("Call Supervisor")
        }
    }
}

@Composable
fun InfoProductionLogs(modifier: Modifier, viewmodel: TcpViewModel){
    val responseLogs by viewmodel.responseLogs.collectAsState()
    val lastLog = responseLogs.lastOrNull()
    val avgResponse = if (responseLogs.isNotEmpty()) responseLogs.map { it.responseTime }.average() else 0.0
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Response Time Monitor",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            MetricCard(
                title = "Last Response",
                value = lastLog?.responseTime?.let { "${"%.2f".format(it)} s" } ?: "--"
            )

            MetricCard(
                title = "Average",
                value = "${"%.2f".format(avgResponse)} s"
            )

            MetricCard(
                title = "Events",
                value = responseLogs.size.toString()
            )
        }

        Spacer(modifier.height(20.dp))

        Text(
            text = "Recent Events",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Los eventos se muestran al revés para ver primero los más recientes.
            items(responseLogs.reversed()) { log ->
                ResponseLogItem(log)
            }
        }
    }
}

@Composable
fun InfoProductionStop(modifier: Modifier, viewmodel: TcpViewModel){
    val stopLogs by viewmodel.stopLogs.collectAsState()
    val lastLog = stopLogs.lastOrNull()
    val avgStopLineTime = if (stopLogs.isNotEmpty()) stopLogs.map { it.stopTime }.average() else 0.0
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Mide tiempos de parada reportados por la línea/estación.

        Text(
            text = "Stop Line Time Monitor",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            MetricCard(
                title = "Last stop line",
                value = lastLog?.stopTime?.let { "${"%.2f".format(it)} s" } ?: "--"
            )

            MetricCard(
                title = "Average",
                value = "${"%.2f".format(avgStopLineTime)} s"
            )

            MetricCard(
                title = "Events",
                value = stopLogs.size.toString()
            )
        }

        Spacer(modifier.height(20.dp))

        Text(
            text = "Recent Events",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stopLogs.reversed()) { log ->
                StopLogItem(log)
            }
        }
    }
}

@Composable
fun InfoRejectionLogs(modifier: Modifier, viewmodel: TcpViewModel){
    val rejectionLogs by viewmodel.rejectionLogs.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Panel de rechazos para mostrar incidencias de calidad o producción.

        Text(
            text = "Part Rejection",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            MetricCard(
                title = "Last part rejected",
                value = "TODO"
            )

            MetricCard(
                title = "Average parts rejected per turn",
                value = "TODO"
            )

            MetricCard(
                title = "Events",
                value = rejectionLogs.size.toString()
            )
        }

        Spacer(modifier.height(20.dp))

        Text(
            text = "Recent Events",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rejectionLogs.reversed()) { log ->
                rejectionLogItem(log)
            }
        }
    }
}

private fun LazyItemScope.rejectionLogItem(log: RejectionLogs) {
    TODO("Not yet implemented")
}


@Composable
fun MetricCard(
    title: String,
    value: String
) {
    // Tarjeta compacta reutilizable para KPIs.
    Card(
        modifier = Modifier.width(110.dp),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun ResponseLogItem(log: ResponseLog) {
    // Fila con la información relevante de un evento de respuesta.

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = log.test,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = log.station,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = log.timestamp,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "${"%.2f".format(log.responseTime)} s",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun StopLogItem(log: StopLog) {
    // Fila con la información relevante de una parada.

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = log.test,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = log.station,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = log.timestamp,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "${"%.2f".format(log.stopTime)} s",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


data class TabItemProduction(
    val title: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
)
