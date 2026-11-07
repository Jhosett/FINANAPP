package com.ufpso.finanapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.ufpso.finanapp.ui.keys.*
import com.ufpso.finanapp.ui.screens.*

@Composable
fun FinanApp(modifier: Modifier = Modifier) {
    val backStack = remember {
        mutableStateListOf<Any>(DashboardRoute)
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryProvider = { key ->
            when (key) {
                // Definición de la Ruta de Login
                is LoginRoute -> NavEntry(key) {
                    LoginScreen(
                        onLoginSuccess = {
                            // Al iniciar sesión limpiamos el stack o añadimos el dashboard
                            backStack.clear()
                            backStack.add(DashboardRoute)
                        },
                        onNavigateToRegister = {
                            backStack.add(RegisterRoute)
                        }
                    )
                }
                // Definición de la Ruta de Register
                is RegisterRoute -> NavEntry(key) {
                    RegisterScreen(
                        onBackToLogin = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                // Definición de la Ruta de Dashboard
                is DashboardRoute -> NavEntry(key) {
                    DashboardScreen(
                        onNavigateToRegister = { backStack.add(RegisterMovementRoute) },
                        onNavigateToManagement = { backStack.add(ManagementRoute) },
                        onNavigateToStatistics = { backStack.add(StatisticsRoute) }
                    )
                }
                // Definición de la Ruta de Registro de movimientos
                is RegisterMovementRoute -> NavEntry(key) {
                    RegisterMovementScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                // Definición de la Ruta de Gestión
                is ManagementRoute -> NavEntry(key) {
                    ManagementScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                // Definición de la Ruta de Estadísticas
                is StatisticsRoute -> NavEntry(key) {
                    StatisticsScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                else -> error("Ruta desconocida: $key")
            }
        }
    )
}
