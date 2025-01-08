package com.example.fittrackerapp.daoInterfaces

import androidx.room.Query
import com.example.fittrackerapp.entities.Category
import kotlinx.coroutines.flow.Flow

interface CategoryDao {

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getById(categoryId: Int): Category?

    @Query("SELECT * FROM categories")
    suspend fun getAll(): Flow<List<Category>>
}