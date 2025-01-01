package com.example.fittrackerapp.daoInterfaces

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fittrackerapp.entities.CompletedExercise

interface CompletedExerciseDao {

    @Insert(entity = CompletedExercise::class)
    suspend fun insertNewCompletedExercise(completedExercise: CompletedExercise)

    @Query("SELECT * FROM completed_exercises WHERE id = :completedExerciseId")
    suspend fun getCompletedExerciseById(completedExerciseId: Int): CompletedExercise?

    @Delete
    suspend fun deleteWorkout(completedExercise: CompletedExercise)

}