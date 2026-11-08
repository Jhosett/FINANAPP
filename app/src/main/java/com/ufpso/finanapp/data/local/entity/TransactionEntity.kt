package com.ufpso.finanapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            // Si se borra un usuario, se borran también sus movimientos
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,              // dueño del movimiento
    val description: String,
    val amount: Double,           // siempre positivo; el tipo define si suma o resta
    val isIncome: Boolean,        // true = ingreso, false = gasto
    val category: String,
    val dateEpochDay: Long,       // fecha del movimiento (LocalDate.toEpochDay())
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)