package com.example.fittrackerapp.classes.daointerfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.fittrackerapp.classes.WorkoutWithExercises
import com.example.fittrackerapp.entities.Workout

@Dao
interface WorkoutWithExercisesDao {
    @Transaction // Обязательно для работы с @Relation
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutWithExercises(workoutId: Int): WorkoutWithExercises?

    @Transaction
    @Query("SELECT * FROM workouts")
    suspend fun getAllWorkoutsWithExercises(): List<WorkoutWithExercises>
}