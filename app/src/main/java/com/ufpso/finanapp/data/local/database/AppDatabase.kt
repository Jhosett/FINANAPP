package com.ufpso.finanapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ufpso.finanapp.data.local.dao.TransactionDao
import com.ufpso.finanapp.data.local.dao.UserDao
import com.ufpso.finanapp.data.local.entity.TransactionEntity
import com.ufpso.finanapp.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, TransactionEntity::class],
    version = 2, // subió de 1 a 2 porque agregamos la tabla transactions
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Crea la tabla nueva SIN borrar los usuarios que ya existen
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` INTEGER NOT NULL,
                        `description` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `isIncome` INTEGER NOT NULL,
                        `category` TEXT NOT NULL,
                        `dateEpochDay` INTEGER NOT NULL,
                        `note` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`userId`) REFERENCES `users`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_transactions_userId` ON `transactions` (`userId`)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finanapp.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}