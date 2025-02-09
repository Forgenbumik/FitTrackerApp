package com.example.fittrackerapp.classes.daointerfaces

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.fittrackerapp.classes.CompletedExerciseWithSets

@Dao
interface CompletedExerciseWithSetsDao {

    @Transaction
    @Query("SELECT * FROM completed_exercises WHERE id = :completedExerciseId")
    suspend fun getById(completedExerciseId: Int): CompletedExerciseWithSets

    @Transaction
    @Query("SELECT * FROM completed_exercises")
    suspend fun getAll(): List<CompletedExerciseWithSets>
}