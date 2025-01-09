package com.example.fittrackerapp.classes.daointerfaces

import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

interface CompletedWorkoutWithExercisesDao {
    @Transaction // Обязательно для работы с @Relation
    @Query("SELECT * FROM completed_workouts WHERE id = :workoutId")
    suspend fun getWorkoutWithExercises(workoutId: Int): CompletedWorkoutWithExercisesDao?

    @Transaction
    @Query("SELECT * FROM completed_workouts")
    suspend fun getAllWorkoutsWithExercises(): Flow<List<CompletedWorkoutWithExercisesDao>>
}