package com.cia.tcpapp.ui.tcpSecondScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cia.tcpapp.TcpViewModel


@Composable
fun InfoMaterials(modifier: Modifier, viewModel: TcpViewModel) {
    // Selector simple de materiales de prueba para pedir información al servidor.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(12.dp)
        ) {
            Button(
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.sendMessage("REQUEST|MATERIAL|1") },
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(text = "Material 1")
            }

            Button(
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.sendMessage("REQUEST|MATERIAL|2") },
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(text = "Material 2")
            }

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .weight(1f)
        ) {
            Button(
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.sendMessage("REQUEST|MATERIAL|3") },
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(text = "Material 3")
            }

            Button(
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.sendMessage("REQUEST|MATERIAL|4") },
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(text = "Material 4")
            }

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .weight(1f)
        ) {
            Button(
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.sendMessage("REQUEST|MATERIAL|5") },
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(text = "Material 5")
            }

            Button(
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.sendMessage("REQUEST|MATERIAL|6") },
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(text = "Material 6")
            }

        }

    }

}
