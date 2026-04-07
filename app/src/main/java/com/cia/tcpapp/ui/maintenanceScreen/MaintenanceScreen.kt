package com.cia.tcpapp.ui.maintenanceScreen


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cia.tcpapp.TcpViewModel


@Composable
fun MaintenanceScreen(
    modifier: Modifier = Modifier,
    viewModel: TcpViewModel
) {
    // Pantalla dedicada al flujo de mantenimiento tras login.
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MaintenanceScreenContent(modifier = modifier, viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreenContent(modifier: Modifier = Modifier, viewModel: TcpViewModel) {
    val info by viewModel.info.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val options by viewModel.maintenanceOptions.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(options[0]) }

    Column(modifier = modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .weight(66f)
        ) {
            // Muestra el detalle textual recibido desde el servidor.
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
            // Permite elegir la comprobación concreta sobre la que se va a actuar.
            OutlinedTextField(
                value = selected.text,
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
                    // Placeholder para enviar confirmación positiva al servidor.
                    viewModel.sendMessage("")
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
                    // Placeholder para reportar fallo de mantenimiento.
                    viewModel.sendMessage("")
                },
                enabled = isConnected && selected != options[0]
            ) {
                Text(text = "Failed")
            }
        }
    }
}
