package com.ufpso.finanapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    // unique = true impide dos usuarios con el mismo usuario o correo
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val email: String,
    val username: String,
    val passwordHash: String, // nunca la contraseña en texto plano
    val createdAt: Long = System.currentTimeMillis()
)