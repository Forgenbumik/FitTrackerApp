package com.example.fittrackerapp.classes.daointerfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fittrackerapp.entities.Workout

@Dao
interface WorkoutWithExercisesDao {

    @Insert(entity = Workout::class)
    suspend fun insertNewWorkout(workout: Workout)

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Int): Workout?

    @Delete
    suspend fun deleteWorkout(workout: Workout)

}