package com.ufpso.finanapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ufpso.finanapp.data.local.database.AppDatabase
import com.ufpso.finanapp.data.local.entity.TransactionEntity
import com.ufpso.finanapp.data.repository.TransactionRepository
import com.ufpso.finanapp.ui.screens.CategoryStatUi
import com.ufpso.finanapp.ui.screens.DailyStatUi
import com.ufpso.finanapp.ui.screens.MovementDetail
import com.ufpso.finanapp.ui.screens.MovementUi
import com.ufpso.finanapp.ui.screens.NewMovement
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Lo que necesita el Dashboard
data class DashboardUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val lastMovements: List<MovementUi> = emptyList()
)

// Lo que necesita la pantalla de Estadísticas
data class StatisticsUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categories: List<CategoryStatUi> = emptyList(),
    val weekly: List<DailyStatUi> = emptyList() // últimos 7 días
)

// Lo que necesita la pantalla de Historial (Gestión)
data class ManagementUiState(
    val movements: List<MovementDetail> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
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
                val today = LocalDate.now()
                val fromDay = today.minusDays(6).toEpochDay()
                combine(
                    repository.observeTotalIncome(userId),
                    repository.observeTotalExpense(userId),
                    repository.observeExpenseByCategory(userId),
                    repository.observeDailyTotals(userId, fromDay, today.toEpochDay())
                ) { income, expense, byCategory, daily ->
                    val totalsByDay = daily.associateBy { it.dateEpochDay }
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
                        },
                        // Siempre 7 días: los que no tienen movimientos quedan en 0
                        weekly = (0..6).map { offset ->
                            val date = today.minusDays(6L - offset)
                            val totals = totalsByDay[date.toEpochDay()]
                            DailyStatUi(
                                date = date,
                                income = totals?.income ?: 0.0,
                                expense = totals?.expense ?: 0.0
                            )
                        }
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val managementState: StateFlow<ManagementUiState> = currentUserId
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(ManagementUiState())
            } else {
                repository.observeAll(userId).map { list ->
                    ManagementUiState(
                        movements = list.map { it.toDetail() },
                        totalIncome = list.filter { it.isIncome }.sumOf { it.amount },
                        totalExpense = list.filter { !it.isIncome }.sumOf { it.amount }
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManagementUiState())

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

    fun updateMovement(id: Int, movement: NewMovement) {
        val userId = currentUserId.value ?: return
        viewModelScope.launch {
            repository.updateTransaction(
                id = id,
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

    fun deleteMovement(id: Int) {
        val userId = currentUserId.value ?: return
        viewModelScope.launch { repository.deleteTransaction(id, userId) }
    }
}

private val dayFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale("es", "CO"))

// Entidad (Room) -> modelo corto del Dashboard
private fun TransactionEntity.toUi() = MovementUi(
    id = id,
    title = description,
    category = category,
    date = LocalDate.ofEpochDay(dateEpochDay).format(dayFormatter),
    amount = amount,
    isIncome = isIncome
)

// Entidad (Room) -> modelo completo de la pantalla de Historial
private fun TransactionEntity.toDetail() = MovementDetail(
    id = id,
    description = description,
    amount = amount,
    isIncome = isIncome,
    category = category,
    date = LocalDate.ofEpochDay(dateEpochDay),
    note = note
)