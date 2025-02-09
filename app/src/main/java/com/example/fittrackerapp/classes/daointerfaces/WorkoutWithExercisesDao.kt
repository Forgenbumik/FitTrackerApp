package com.example.fittrackerapp.classes.daointerfaces

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.fittrackerapp.classes.WorkoutWithExercises
import kotlinx.coroutines.flow.StateFlow

@Dao
interface WorkoutWithExercisesDao {
    @Transaction // Обязательно для работы с @Relation
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getById(workoutId: Int): WorkoutWithExercises?

    @Transaction
    @Query("SELECT * FROM workouts")
    suspend fun getAll(): List<WorkoutWithExercises>
}