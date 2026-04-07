package com.cia.tcpapp.ui.tcpScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cia.tcpapp.TcpViewModel
import com.cia.tcpapp.TcpViewModel.ScreenType

@Composable
fun TcpScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMaterials: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TcpViewModel,
    setFab: ((@Composable () -> Unit)?) -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TcpScreenContent(
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToMaterials = onNavigateToMaterials,
            viewModel = viewModel,
            setFab = setFab
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TcpScreenContent(
    onNavigateToLogin: () -> Unit,
    onNavigateToMaterials: () -> Unit,
    viewModel: TcpViewModel,
    setFab: ((@Composable () -> Unit)?) -> Unit
) {

    val isConnected by viewModel.isConnected.collectAsState()
    val activeTest by viewModel.activeTest.collectAsState()

    LaunchedEffect(isConnected) {
        // Mientras no hay conexión, la pantalla principal mantiene activa la búsqueda NSD.
        if (!isConnected) {
            viewModel.startDiscovery()
        } else {
            viewModel.stopDiscovery()
        }
    }

    LaunchedEffect(Unit) {
        // La FAB se define desde esta pantalla para reutilizar el Scaffold principal.
        setFab {
            ExpandableFab(
                onAction1 = {
                    viewModel.setScreenType(ScreenType.TestFabBuild)
                    viewModel.sendMessage("MAINTENANCE|${viewModel.screenType.value}\n")
                    viewModel.requestLoginFor(TcpViewModel.PostLoginDestination.MaintenanceScreen)
                    onNavigateToLogin()
                },
                onAction2 = {
                },
                viewModel = viewModel
            )
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(33f),
            horizontalArrangement = Arrangement.spacedBy(50.dp)
        ) {
            ToggleBlinkButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                enabled = isConnected && (activeTest == null || activeTest == ScreenType.MANTENIMIENTO),
                text = "MANTENIMIENTO",
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF35831),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp)
            ) { isBlinking ->
                // Cada botón alterna entre iniciar una prueba y detenerla para entrar al detalle.
                if (isBlinking) {
                    viewModel.setScreenType(ScreenType.MANTENIMIENTO)
                    viewModel.selectTest(ScreenType.MANTENIMIENTO)
                    viewModel.sendMessage("START|MANTENIMIENTO")
                } else {
                    viewModel.clearTest()
                    viewModel.sendMessage("STOP|MANTENIMIENTO")
                    viewModel.requestLoginFor(
                        TcpViewModel.PostLoginDestination.SecondScreen(
                            ScreenType.MANTENIMIENTO.name
                        )
                    )
                    onNavigateToLogin()
                }
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                enabled = isConnected && (activeTest == null || activeTest == ScreenType.MATERIALES),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF34292),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp),
                onClick = {
                    viewModel.selectTest(ScreenType.MATERIALES)
                    viewModel.setScreenType(ScreenType.MATERIALES)
                    onNavigateToMaterials()
                }
            ) {
                Text(
                    "MATERIALES",
                    fontSize = 20.sp
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(33f),
            horizontalArrangement = Arrangement.spacedBy(50.dp)
        ) {
            ToggleBlinkButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                enabled = isConnected && (activeTest == null || activeTest == ScreenType.CALIDAD),
                text = "CALIDAD",
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00B8D4),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp)
            ) { isBlinking ->
                if (isBlinking) {
                    viewModel.selectTest(ScreenType.CALIDAD)
                    viewModel.sendMessage("START|CALIDAD")
                } else {
                    viewModel.clearTest()
                    viewModel.setScreenType(ScreenType.CALIDAD)
                    viewModel.sendMessage("STOP|CALIDAD")
                    viewModel.requestLoginFor(
                        TcpViewModel.PostLoginDestination.SecondScreen(
                            ScreenType.CALIDAD.name
                        )
                    )
                    onNavigateToLogin()
                }
            }
            ToggleBlinkButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                enabled = isConnected && (activeTest == null || activeTest == ScreenType.PRODUCCION),
                text = "PRODUCCION",
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BFA5),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp)
            ) { isBlinking ->
                if (isBlinking) {
                    viewModel.selectTest(ScreenType.PRODUCCION)
                    viewModel.sendMessage("START|PRODUCCION")
                } else {
                    viewModel.clearTest()
                    viewModel.setScreenType(ScreenType.PRODUCCION)
                    viewModel.sendMessage("STOP|PRODUCCION")
                    viewModel.requestLoginFor(
                        TcpViewModel.PostLoginDestination.SecondScreen(
                            ScreenType.PRODUCCION.name
                        )
                    )
                    onNavigateToLogin()
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(33f),
            horizontalArrangement = Arrangement.spacedBy(50.dp)
        ) {
            ToggleBlinkButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                enabled = isConnected && (activeTest == null || activeTest == ScreenType.Test5),
                text = "Test 5",
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFAC75C),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp)
            ) { isBlinking ->
                if (isBlinking) {
                    viewModel.selectTest(ScreenType.Test5)
                    viewModel.sendMessage("START|Test5")
                } else {
                    viewModel.clearTest()
                    viewModel.setScreenType(ScreenType.Test5)
                    viewModel.sendMessage("STOP|Test5")
                    viewModel.requestLoginFor(
                        TcpViewModel.PostLoginDestination.SecondScreen(
                            ScreenType.Test5.name
                        )
                    )
                    onNavigateToLogin()
                }
            }
            ToggleBlinkButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                enabled = isConnected && (activeTest == null || activeTest == ScreenType.Test6),
                text = "Test 6",
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCF44E7),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp)
            ) { isBlinking ->
                if (isBlinking) {
                    viewModel.selectTest(ScreenType.Test6)
                    viewModel.sendMessage("START|Test6")
                } else {
                    viewModel.clearTest()
                    viewModel.setScreenType(ScreenType.Test6)
                    viewModel.sendMessage("STOP|Test6")
                    viewModel.requestLoginFor(
                        TcpViewModel.PostLoginDestination.SecondScreen(
                            ScreenType.Test6.name
                        )
                    )
                    onNavigateToLogin()
                }
            }
        }

    }
}

@Composable
fun ToggleBlinkButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    colors: ButtonColors,
    shape: Shape,
    onToggle: (Boolean) -> Unit
) {
    var blinking by rememberSaveable { mutableStateOf(false) }

    val alpha = if (blinking) {
        val infiniteTransition = rememberInfiniteTransition(label = "blink")
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        ).value
    } else {
        1f
    }

    Button(
        modifier = modifier.graphicsLayer { this.alpha = alpha },
        enabled = enabled,
        onClick = {
            blinking = !blinking
            onToggle(blinking)
        },
        colors = colors,
        shape = shape
    ) {
        Text(
            text,
            fontSize = 20.sp
        )
    }
}

@Composable
fun ExpandableFab(
    onAction1: () -> Unit,
    onAction2: () -> Unit,
    viewModel: TcpViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(visible = expanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        expanded = false
                        onAction1()
                    },
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(100)
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Test")
                }

                FloatingActionButton(
                    onClick = {
                        expanded = false
                        onAction2()
                    },
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(100)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Test")
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (viewModel.isConnected.value) {
                    expanded = !expanded
                }
            },
            modifier = Modifier.size(75.dp),
            shape = RoundedCornerShape(100),

            ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Menú"
            )
        }
    }
}
