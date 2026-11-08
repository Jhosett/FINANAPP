package com.ufpso.finanapp.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.ufpso.finanapp.data.local.dao.UserDao
import com.ufpso.finanapp.data.local.entity.UserEntity
import com.ufpso.finanapp.data.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface AuthResult {
    data class Success(val user: UserEntity) : AuthResult
    data class Error(val message: String) : AuthResult
}

class UserRepository(private val userDao: UserDao) {

    suspend fun register(
        fullName: String,
        email: String,
        username: String,
        password: String
    ): AuthResult {
        // Normalizamos para que "Juan" y "juan " sean el mismo usuario
        val cleanUsername = username.trim().lowercase()
        val cleanEmail = email.trim().lowercase()

        if (userDao.findByUsername(cleanUsername) != null)
            return AuthResult.Error("Ese nombre de usuario ya está en uso")
        if (userDao.findByEmail(cleanEmail) != null)
            return AuthResult.Error("Ese correo ya está registrado")

        // El hash consume CPU a propósito, así que va fuera del hilo principal
        val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(password) }

        val user = UserEntity(
            fullName = fullName.trim(),
            email = cleanEmail,
            username = cleanUsername,
            passwordHash = hash
        )

        return try {
            val id = userDao.insert(user)
            AuthResult.Success(user.copy(id = id.toInt()))
        } catch (e: SQLiteConstraintException) {
            AuthResult.Error("El usuario o el correo ya existen")
        }
    }

    suspend fun login(username: String, password: String): AuthResult {
        val user = userDao.findByUsername(username.trim().lowercase())
            ?: return AuthResult.Error("Usuario o contraseña incorrectos")

        val ok = withContext(Dispatchers.Default) {
            PasswordHasher.verify(password, user.passwordHash)
        }

        // Mismo mensaje en ambos casos para no revelar si el usuario existe
        return if (ok) AuthResult.Success(user)
        else AuthResult.Error("Usuario o contraseña incorrectos")
    }
}