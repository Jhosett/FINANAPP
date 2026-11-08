package com.ufpso.finanapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ufpso.finanapp.data.local.dao.UserDao
import com.ufpso.finanapp.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class], // aquí irán después las demás tablas
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton: una sola instancia de la BD en toda la app
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finanapp.db"
                ).build().also { INSTANCE = it }
            }
    }
}