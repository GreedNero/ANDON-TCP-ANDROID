package com.cia.tcpapp.ui.tcpLoginScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cia.tcpapp.AppRoutes
import com.cia.tcpapp.TcpViewModel


@Composable
fun LoginScreen(
    viewModel: TcpViewModel,
    navController: NavController
) {
    val field1 by viewModel.field1.collectAsState()
    val field2 by viewModel.field2.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val loginResult by viewModel.loginResult.collectAsState()
    val destination by viewModel.postLoginDestination.collectAsState()

    LaunchedEffect(loginResult) {
        // Cuando el servidor confirma el login, redirige a la pantalla que se pidió antes.
        if (loginResult == TcpViewModel.LoginResult.Success && destination != null) {

            when (val dest = destination) {

                is TcpViewModel.PostLoginDestination.SecondScreen -> {
                    navController.navigate("SecondScreen/${dest.screenType}") {
                        popUpTo(AppRoutes.LoginScreen) { inclusive = true }
                    }
                }

                TcpViewModel.PostLoginDestination.MaintenanceScreen -> {
                    navController.navigate(AppRoutes.MaintenanceScreen) {
                        popUpTo(AppRoutes.LoginScreen) { inclusive = true }
                    }
                }

                else -> {}
            }

            viewModel.clearLoginResult()
            viewModel.clearPostLoginDestination()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pantalla simple para capturar credenciales antes de abrir la vista protegida.
        Text(text = "Login Screen Test", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            modifier = Modifier.padding(top = 16.dp),
            value = field1,
            onValueChange = viewModel::onFieldChange,
            label = { Text("Username") },
            singleLine = true
        )
        OutlinedTextField(
            modifier = Modifier.padding(top = 16.dp),
            value = field2,
            onValueChange = viewModel::onFieldChange2,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrectEnabled = false
            )

        )
        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = {
                viewModel.sendData()
            },
            enabled = isConnected && field1.isNotEmpty() && field2.isNotEmpty()
        ) {
            Text(text = "Login")
        }
    }
}
