package com.cia.tcpapp.ui.tcpSecondScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cia.tcpapp.TcpViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun InfoMaintenance(
    modifier: Modifier = Modifier,
    viewModel: TcpViewModel
) {
    // Vista de mantenimiento con dos pestañas: datos recibidos y PDF asociado.
    val items = listOf(
        TabItem(
            title = "Tab Info",
            unselectedIcon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home
        ),
        TabItem(
            title = "Tab PDF",
            unselectedIcon = Icons.Outlined.PictureAsPdf,
            selectedIcon = Icons.Filled.PictureAsPdf
        )
    )

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {

        PrimaryTabRow(selectedTabIndex = selectedTabIndex, Modifier
            .zIndex(1f)
            .fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                androidx.compose.material3.Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(item.title) },
                    icon = {
                        androidx.compose.material3.Icon(
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
                MaintenanceInfoContent(
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel
                )
            }

            1 -> {
                PdfContent(
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceInfoContent(modifier: Modifier = Modifier, viewModel: TcpViewModel) {

    val info by viewModel.info.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val options by viewModel.maintenanceOptions.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var selected by remember(options) {
        mutableStateOf(options.firstOrNull())
    }

    Column(modifier = modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .weight(66f)
        ) {
            // Renderiza el estado de mantenimiento en formato monoespaciado.
            LazyColumn(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                items(info) { msg ->
                    Text(
                        msg.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp
                    )
                }
            }

        }

        ExposedDropdownMenuBox(
            modifier = Modifier
                .padding(8.dp),
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            // Al seleccionar una opción se pide también el PDF relacionado.
            OutlinedTextField(
                value = selected?.text ?: "None",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                    enabled = isConnected
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.text) },
                        onClick = {
                            selected = option
                            expanded = false
                            viewModel.sendMessage("SEND_PDF|test.pdf")
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .weight(20f),
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxHeight()
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF21E50B),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp),
                onClick = {
                    viewModel.sendMessage(
                        "STATUS_MAINTENANCE|$selected|state=OK|last_checked=${
                            LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                        }")
                    viewModel.sendMessage("STOP_LINE|{${viewModel.screenType.value}")
                },
                enabled = isConnected && selected != options[0]
            ) {
                Text(text = "Ok")
            }
            Button(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxHeight()
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE5370B),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp),
                onClick = {
                    viewModel.sendMessage(
                        "STATUS_MAINTENANCE|$selected|state=failed|last_checked=${
                            LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                        }"
                    )
                },
                enabled = isConnected && selected != options[0]
            ) {
                Text(text = "Failed")
            }
        }
    }
}

data class TabItem(
    val title: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
)

