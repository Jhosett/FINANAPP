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

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND isIncome = 1")
    fun observeTotalIncome(userId: Int): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND isIncome = 0")
    fun observeTotalExpense(userId: Int): Flow<Double>
}