package com.ufpso.finanapp.ui.viewmodel

import com.ufpso.finanapp.ui.screens.MovementUi

data class CategoryExpense(
    val category: String,
    val total: Double,
    val percentage: Float // 0f..1f
)

fun calculateExpenseByCategory(movements: List<MovementUi>): List<CategoryExpense> {
    val expenses = movements.filter { !it.isIncome && it.amount > 0.0 }
    val total = expenses.sumOf { it.amount }
    if (total <= 0.0) return emptyList()

    return expenses
        .groupBy { it.category.trim().lowercase().ifBlank { "sin categoría" } }
        .map { (_, items) ->
            val sum = items.sumOf { it.amount }
            CategoryExpense(
                category = items.first().category.trim().ifBlank { "Sin categoría" },
                total = sum,
                percentage = (sum / total).toFloat()
            )
        }
        .sortedByDescending { it.total }
}