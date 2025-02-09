package com.example.fittrackerapp.classes.daointerfaces

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.fittrackerapp.classes.CompletedWorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedWorkoutWithExercisesDao {
    @Transaction // Обязательно для работы с @Relation
    @Query("SELECT * FROM completed_workouts WHERE id = :workoutId")
    suspend fun getById(workoutId: Int): CompletedWorkoutWithExercises?

    @Transaction
    @Query("SELECT * FROM completed_workouts")
    suspend fun getAll(): List<CompletedWorkoutWithExercises>
}