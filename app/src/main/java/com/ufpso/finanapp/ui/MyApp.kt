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
                is DashboardRoute -> NavEntry(key) {
                    DashboardScreen(
                        onNavigateToRegister = { backStack.add(RegisterMovementRoute) },
                        onNavigateToManagement = { backStack.add(ManagementRoute) },
                        onNavigateToStatistics = { backStack.add(StatisticsRoute) }
                    )
                }
                is RegisterMovementRoute -> NavEntry(key) {
                    RegisterMovementScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                is ManagementRoute -> NavEntry(key) {
                    ManagementScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
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
