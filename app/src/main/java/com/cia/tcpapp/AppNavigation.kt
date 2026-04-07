package com.cia.tcpapp

import androidx.compose.ui.platform.LocalContext
import android.app.Application
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cia.tcpapp.ui.tcpLoginScreen.LoginScreen
import com.cia.tcpapp.ui.tcpScreen.TcpScreen
import com.cia.tcpapp.ui.tcpSecondScreen.SecondScreen
import com.cia.tcpapp.ui.maintenanceScreen.MaintenanceScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    setFab: ((@Composable () -> Unit)?) -> Unit,
    snackbarHostState: SnackbarHostState) {
    // Se comparte una sola instancia del ViewModel entre todas las pantallas.
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as Application
    val tcpViewModel: TcpViewModel = viewModel(
        factory = AppViewModelFactory(application)
    )

    NavHost(
        navController = navController,
        startDestination = AppRoutes.TcpMainScreen,
        modifier = modifier
    ) {
        composable(route = AppRoutes.TcpMainScreen) {
            TcpScreen(
                onNavigateToLogin = {
                    navController.navigate(AppRoutes.LoginScreen)
                },
                onNavigateToMaterials = {
                    navController.navigate("SecondScreen/${tcpViewModel.screenType.value}")
                },
                modifier = Modifier,
                tcpViewModel,
                setFab = setFab
            )
        }

        composable(
            route = "SecondScreen/{screenType}",
            arguments = listOf(
                navArgument("screenType") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val screenType = backStackEntry.arguments?.getString("screenType")!!

            SecondScreen(
                screenType = screenType,
                viewModel = tcpViewModel,
                snackbarHostState = snackbarHostState,
                onExit = {
                    // Al salir se limpian estados temporales y se notifica al servidor.
                    if(tcpViewModel.screenType.value == TcpViewModel.ScreenType.MATERIALES){
                        tcpViewModel.clearPostLoginDestination()
                        tcpViewModel.clearLoginResult()
                        tcpViewModel.clearPdfBytes()
                        tcpViewModel.clearTest()
                        tcpViewModel.sendMessage("STOP|$screenType")
                        tcpViewModel.sendMessage("STATUS|OK")
                        navController.popBackStack()

                    }
                    else{
                        tcpViewModel.clearPostLoginDestination()
                        tcpViewModel.clearLoginResult()
                        tcpViewModel.clearPdfBytes()
                        tcpViewModel.sendMessage("STATUS|OK")
                        navController.popBackStack()
                    }

                }
            )
        }

        composable(
            route = AppRoutes.LoginScreen,
        ) {
            LoginScreen(
                tcpViewModel,
                navController
            )
        }
        composable(
            route = AppRoutes.MaintenanceScreen,
        ) {
            MaintenanceScreen(
                modifier = Modifier,
                tcpViewModel
            )
        }
    }

}

