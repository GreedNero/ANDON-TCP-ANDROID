package com.cia.tcpapp.ui.tcpSecondScreen


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cia.tcpapp.TcpViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun SecondScreen(
    screenType: String,
    modifier: Modifier = Modifier,
    viewModel: TcpViewModel,
    snackbarHostState: SnackbarHostState,
    onExit: () -> Unit
) {
    var backPressedOnce by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    LaunchedEffect(backPressedOnce) {
        // La confirmación de salida dura 2 segundos antes de resetearse.
        if (backPressedOnce) {
            delay(2000)
            backPressedOnce = false
        }
    }


    BackHandler {
        // Requiere doble pulsación para evitar salir por accidente de una vista operativa.
        if (backPressedOnce) {
            onExit()
        } else {
            backPressedOnce = true

            scope.launch {
                snackbarHostState.showSnackbar(
                    "Presiona atrás otra vez para salir"
                )
            }
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Según el tipo de estación/prueba, muestra la vista específica.
        when (screenType) {
            "MANTENIMIENTO" -> InfoMaintenance(modifier=Modifier,viewModel)
            "MATERIALES" -> InfoMaterials(modifier=Modifier,viewModel)
            "CALIDAD" -> InfoQuality(modifier=Modifier,viewModel)
            "PRODUCCION" -> InfoProduction(modifier= Modifier, viewModel)
            "Test5" -> InfoTest5(viewModel)
            "Test6" -> InfoTest6(viewModel)
        }
    }
}

