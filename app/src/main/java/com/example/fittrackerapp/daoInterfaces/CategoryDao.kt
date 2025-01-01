package com.example.fittrackerapp.daoInterfaces

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fittrackerapp.entities.Category

interface CategoryDao {
    @Insert(entity = Category::class)
    suspend fun insertNewCategory(category: Category)

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getWorkoutById(categoryId: Int): Category?

    @Delete
    suspend fun deleteCategory(category: Category)
}