package com.example.financeapp.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CategoryDao {
    @Insert
    suspend fun insert(item: CategoryEntity): Long

    @Delete
    suspend fun delete(item: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>
}
