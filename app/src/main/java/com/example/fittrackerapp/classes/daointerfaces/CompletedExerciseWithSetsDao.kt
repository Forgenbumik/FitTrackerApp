package com.example.fittrackerapp.classes.daointerfaces

import androidx.room.Query
import androidx.room.Transaction
import com.example.fittrackerapp.classes.CompletedExerciseWithSets

interface CompletedExerciseWithSetsDao {

    @Transaction
    @Query("SELECT * FROM completed_exercises WHERE id = :completedExerciseId")
    suspend fun getCompletedExerciseWithSets(completedExerciseId: Int): CompletedExerciseWithSets

    @Transaction
    @Query("SELECT * FROM completed_exercises")
    suspend fun getAllWorkoutsWithExercises(): List<CompletedExerciseWithSets>
}