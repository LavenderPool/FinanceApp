package com.example.financeapp.data

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver

private const val DATABASE_NAME = "finance.db"

internal object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder<AppDatabase>(
            context.applicationContext,
            DATABASE_NAME,
        ).setDriver(AndroidSQLiteDriver())
            .addMigrations(MIGRATION_1_2)
            .build()
            .also { instance = it }
    }
}
