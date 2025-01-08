package com.example.fittrackerapp.daoInterfaces

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fittrackerapp.entities.Category
import com.example.fittrackerapp.entities.CompletedExercise
import kotlinx.coroutines.flow.Flow

interface CompletedExerciseDao {

    @Insert(entity = CompletedExercise::class)
    suspend fun insert(completedExercise: CompletedExercise)

    @Query("SELECT * FROM completed_exercises WHERE id = :completedExerciseId")
    suspend fun getById(completedExerciseId: Int): CompletedExercise?

    @Delete
    suspend fun delete(completedExercise: CompletedExercise)

    @Update
    suspend fun update(completedExercise: CompletedExercise)

}