package com.ufpso.finanapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.ufpso.finanapp.ui.keys.*
import com.ufpso.finanapp.ui.screens.*
import com.ufpso.finanapp.ui.viewmodel.AuthViewModel
import com.ufpso.finanapp.ui.viewmodel.MovementViewModel

@Composable
fun FinanApp(modifier: Modifier = Modifier) {
    val backStack = remember {
        mutableStateListOf<Any>(LoginRoute) // empieza en el login
    }

    // Un solo ViewModel compartido por Login y Register
    val authViewModel: AuthViewModel = viewModel()
    val movementViewModel: MovementViewModel = viewModel()
    val dashboardState by movementViewModel.dashboardState.collectAsState()

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryProvider = { key ->
            when (key) {
                is LoginRoute -> NavEntry(key) {
                    LoginScreen(
                        onLogin = { username, password ->
                            authViewModel.login(username, password) {
                                movementViewModel.setUser(authViewModel.currentUser?.id)
                                backStack.clear()
                                backStack.add(DashboardRoute)
                            }
                        },
                        loginError = authViewModel.loginError,
                        onNavigateToRegister = {
                            authViewModel.clearErrors()
                            backStack.add(RegisterRoute)
                        }
                    )
                }

                is RegisterRoute -> NavEntry(key) {
                    RegisterScreen(
                        onRegister = { fullName, email, username, password ->
                            authViewModel.register(fullName, email, username, password) {
                                // Éxito: volvemos al login para que inicie sesión
                                backStack.removeLastOrNull()
                            }
                        },
                        registerError = authViewModel.registerError,
                        onBackToLogin = {
                            authViewModel.clearErrors()
                            backStack.removeLastOrNull()
                        }
                    )
                }

                // Las demás rutas quedan igual que las tenías
                is DashboardRoute -> NavEntry(key) {
                    DashboardScreen(
                        onNavigateToRegister = { backStack.add(RegisterMovementRoute) },
                        onNavigateToManagement = { backStack.add(ManagementRoute) },
                        onNavigateToStatistics = { backStack.add(StatisticsRoute) },
                        userName = authViewModel.currentUser?.fullName?.substringBefore(" ") ?: "",
                        balance = dashboardState.balance,
                        totalIncome = dashboardState.totalIncome,
                        totalExpense = dashboardState.totalExpense,
                        lastMovements = dashboardState.lastMovements
                    )
                }
                is RegisterMovementRoute -> NavEntry(key) {
                    RegisterMovementScreen(
                        onBack = { backStack.removeLastOrNull() },
                        onNavigateToManagement = { backStack.add(ManagementRoute) },
                        onNavigateToStatistics = { backStack.add(StatisticsRoute) },
                        onSave = { movementViewModel.saveMovement(it) }
                    )
                }
                is ManagementRoute -> NavEntry(key) {
                    ManagementScreen(onBack = { backStack.removeLastOrNull() })
                }
                is StatisticsRoute -> NavEntry(key) {
                    StatisticsScreen(onBack = { backStack.removeLastOrNull() })
                }
                else -> error("Ruta desconocida: $key")
            }
        }
    )
}
