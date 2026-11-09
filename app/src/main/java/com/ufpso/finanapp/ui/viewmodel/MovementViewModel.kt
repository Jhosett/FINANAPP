package com.ufpso.finanapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ufpso.finanapp.data.local.database.AppDatabase
import com.ufpso.finanapp.data.local.entity.TransactionEntity
import com.ufpso.finanapp.data.repository.TransactionRepository
import com.ufpso.finanapp.ui.screens.MovementUi
import com.ufpso.finanapp.ui.screens.NewMovement
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.ufpso.finanapp.ui.screens.CategoryStatUi

// Lo que necesita el Dashboard
data class DashboardUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val lastMovements: List<MovementUi> = emptyList()
)

class MovementViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(
        AppDatabase.getInstance(application).transactionDao()
    )

    // Usuario que inició sesión (null = nadie)
    private val currentUserId = MutableStateFlow<Int?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val dashboardState: StateFlow<DashboardUiState> = currentUserId
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(DashboardUiState())
            } else {
                combine(
                    repository.observeTotalIncome(userId),
                    repository.observeTotalExpense(userId),
                    repository.observeLatest(userId, limit = 5)
                ) { income, expense, latest ->
                    DashboardUiState(
                        balance = income - expense,
                        totalIncome = income,
                        totalExpense = expense,
                        lastMovements = latest.map { it.toUi() }
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val statisticsState: StateFlow<StatisticsUiState> = currentUserId
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(StatisticsUiState())
            } else {
                combine(
                    repository.observeTotalIncome(userId),
                    repository.observeTotalExpense(userId),
                    repository.observeExpenseByCategory(userId)
                ) { income, expense, byCategory ->
                    StatisticsUiState(
                        balance = income - expense,
                        totalIncome = income,
                        totalExpense = expense,
                        categories = byCategory.map {
                            CategoryStatUi(
                                category = it.category,
                                total = it.total,
                                percentage = if (expense > 0) (it.total / expense).toFloat() else 0f
                            )
                        }
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())
    fun setUser(userId: Int?) {
        currentUserId.value = userId
    }

    fun saveMovement(movement: NewMovement) {
        val userId = currentUserId.value ?: return // sin sesión no se guarda
        viewModelScope.launch {
            repository.addTransaction(
                userId = userId,
                description = movement.description,
                amount = movement.amount,
                isIncome = movement.isIncome,
                category = movement.category,
                dateEpochDay = movement.date.toEpochDay(),
                note = movement.note
            )
        }
    }
}

private val dayFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale("es", "CO"))

// Entidad (Room) -> modelo de pantalla
private fun TransactionEntity.toUi() = MovementUi(
    id = id,
    title = description,
    category = category,
    date = LocalDate.ofEpochDay(dateEpochDay).format(dayFormatter),
    amount = amount,
    isIncome = isIncome
)
data class StatisticsUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categories: List<CategoryStatUi> = emptyList()
)
