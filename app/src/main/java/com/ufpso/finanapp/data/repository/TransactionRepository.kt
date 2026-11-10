package com.ufpso.finanapp.data.repository

import com.ufpso.finanapp.data.local.dao.CategoryTotal
import com.ufpso.finanapp.data.local.dao.TransactionDao
import com.ufpso.finanapp.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    suspend fun addTransaction(
        userId: Int,
        description: String,
        amount: Double,
        isIncome: Boolean,
        category: String,
        dateEpochDay: Long,
        note: String
    ) {
        transactionDao.insert(
            TransactionEntity(
                userId = userId,
                description = description.trim(),
                amount = amount,
                isIncome = isIncome,
                category = category,
                dateEpochDay = dateEpochDay,
                note = note.trim()
            )
        )
    }

    // --- Gestión: editar y eliminar ---
    suspend fun updateTransaction(
        id: Int,
        userId: Int,
        description: String,
        amount: Double,
        isIncome: Boolean,
        category: String,
        dateEpochDay: Long,
        note: String
    ) {
        transactionDao.updateById(
            id = id,
            userId = userId,
            description = description.trim(),
            amount = amount,
            isIncome = isIncome,
            category = category,
            dateEpochDay = dateEpochDay,
            note = note.trim()
        )
    }

    suspend fun deleteTransaction(id: Int, userId: Int) =
        transactionDao.deleteById(id, userId)

    // --- Consultas ---
    fun observeAll(userId: Int): Flow<List<TransactionEntity>> =
        transactionDao.observeAll(userId)

    fun observeLatest(userId: Int, limit: Int = 5): Flow<List<TransactionEntity>> =
        transactionDao.observeLatest(userId, limit)

    fun observeTotalIncome(userId: Int): Flow<Double> =
        transactionDao.observeTotalIncome(userId)

    fun observeTotalExpense(userId: Int): Flow<Double> =
        transactionDao.observeTotalExpense(userId)

    // --- Estadísticas (agregado por tu compañero) ---
    fun observeExpenseByCategory(userId: Int): Flow<List<CategoryTotal>> =
        transactionDao.observeExpenseByCategory(userId)
}