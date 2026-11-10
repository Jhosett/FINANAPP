package com.ufpso.finanapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ufpso.finanapp.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    // Flow: la pantalla se actualiza sola cada vez que cambia la tabla
    @Query(
        """SELECT * FROM transactions
           WHERE userId = :userId
           ORDER BY dateEpochDay DESC, id DESC
           LIMIT :limit"""
    )
    fun observeLatest(userId: Int, limit: Int): Flow<List<TransactionEntity>>

    @Query(
        """SELECT * FROM transactions
           WHERE userId = :userId
           ORDER BY dateEpochDay DESC, id DESC"""
    )
    fun observeAll(userId: Int): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND isIncome = 1")
    fun observeTotalIncome(userId: Int): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND isIncome = 0")
    fun observeTotalExpense(userId: Int): Flow<Double>

    // --- Estadísticas (agregado por tu compañero) ---
    @Query(
        """SELECT category AS category, SUM(amount) AS total
           FROM transactions
           WHERE userId = :userId AND isIncome = 0
           GROUP BY category
           ORDER BY total DESC"""
    )
    fun observeExpenseByCategory(userId: Int): Flow<List<CategoryTotal>>

    // Ingresos y gastos agrupados por día, dentro de un rango de fechas
    @Query(
        """SELECT dateEpochDay AS dateEpochDay,
                  COALESCE(SUM(CASE WHEN isIncome = 1 THEN amount ELSE 0 END), 0.0) AS income,
                  COALESCE(SUM(CASE WHEN isIncome = 0 THEN amount ELSE 0 END), 0.0) AS expense
           FROM transactions
           WHERE userId = :userId AND dateEpochDay BETWEEN :fromDay AND :toDay
           GROUP BY dateEpochDay"""
    )
    fun observeDailyTotals(userId: Int, fromDay: Long, toDay: Long): Flow<List<DailyTotal>>

    // --- Gestión: editar y eliminar ---
    // Se filtra también por userId: un usuario nunca modifica movimientos ajenos
    @Query(
        """UPDATE transactions
           SET description = :description,
               amount = :amount,
               isIncome = :isIncome,
               category = :category,
               dateEpochDay = :dateEpochDay,
               note = :note
           WHERE id = :id AND userId = :userId"""
    )
    suspend fun updateById(
        id: Int,
        userId: Int,
        description: String,
        amount: Double,
        isIncome: Boolean,
        category: String,
        dateEpochDay: Long,
        note: String
    )

    @Query("DELETE FROM transactions WHERE id = :id AND userId = :userId")
    suspend fun deleteById(id: Int, userId: Int)
}

data class CategoryTotal(val category: String, val total: Double)
data class DailyTotal(val dateEpochDay: Long, val income: Double, val expense: Double)