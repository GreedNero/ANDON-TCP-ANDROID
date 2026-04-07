package com.cia.tcpapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Punto de entrada de toda la interfaz Compose.
            ViewContainer()
        }
    }
}

@Composable
fun ViewContainer() {

    var fabContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        // El contenedor principal mantiene la barra superior, snackbar y FAB compartidos.
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { Toolbar() },
        floatingActionButton = {
            fabContent?.invoke()
        }
    ) { innerPadding ->
        AppNavigation(
            modifier = Modifier.padding(innerPadding),
            setFab = { fabContent = it },
            snackbarHostState = snackbarHostState
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar() {
    TopAppBar(
        // Barra superior fija para toda la app.
        title = { Text(text = "TCP App") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Blue,
            titleContentColor = Color.White
        )
    )
}
