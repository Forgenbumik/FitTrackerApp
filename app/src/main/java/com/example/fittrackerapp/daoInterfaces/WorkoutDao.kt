package com.example.fittrackerapp.daoInterfaces

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fittrackerapp.entities.Workout

interface WorkoutDao {
    @Insert(entity = Workout::class)
    suspend fun insertNewWorkoutDetail(workout: Workout)

    @Query("SELECT * FROM workout_details WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Int): Workout?

    @Delete
    suspend fun deleteWorkout(workout: Workout)
}