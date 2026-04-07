package com.cia.tcpapp.ui.tcpSecondScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.saveable.rememberSaveable
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


@Composable
fun InfoQuality(modifier: Modifier = Modifier,viewModel: TcpViewModel){
    // Vista de calidad con detalle del material y acceso al PDF asociado.
    val items = listOf(
        TabItemMaterials(
            title = "Tab Info",
            unselectedIcon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home
        ),
        TabItemMaterials(
            title = "Tab PDF",
            unselectedIcon = Icons.Outlined.PictureAsPdf,
            selectedIcon = Icons.Filled.PictureAsPdf
        )
    )

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {

        PrimaryTabRow(selectedTabIndex = selectedTabIndex, Modifier.zIndex(1f).fillMaxWidth()) {
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
               InfoQualityScreenContent(
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
fun InfoQualityScreenContent(modifier: Modifier, viewModel: TcpViewModel) {

    var comment by rememberSaveable { mutableStateOf("") }
    val materials by viewModel.materials.collectAsState()
    val selectedMaterial by viewModel.selectedMaterial.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val descriptionLines = selectedMaterial
        ?.description
        ?.lines()
        ?: emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Muestra la descripción técnica del material seleccionado.
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(descriptionLines) { line ->
                    Text(
                        text = line,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = modifier.height(16.dp))

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
        ) {

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                // Cambiar de material actualiza el detalle y solicita su PDF.
                OutlinedTextField(
                    value = selectedMaterial?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Material") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    },
                    modifier = modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryEditable
                        )
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    materials.forEach { material ->
                        DropdownMenuItem(
                            text = { Text(material.name) },
                            onClick = {
                                viewModel.selectMaterial(material)
                                viewModel.sendMessage("SEND_PDF|test.pdf")
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = modifier.height(12.dp))

            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    modifier = modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    value = comment,
                    onValueChange = {
                        comment = it
                        viewModel.onFieldChange2(it)
                    },
                    label = { Text("Comentario") }
                )

                Button(
                    onClick = {
                        // Envía un comentario libre asociado a la sesión o material actual.
                        viewModel.sendMessage("COMMENT|$comment\n")
                        comment = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF78E73E),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Enviar")
                }
            }
        }
    }
}


data class TabItemMaterials(
    val title: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
)
