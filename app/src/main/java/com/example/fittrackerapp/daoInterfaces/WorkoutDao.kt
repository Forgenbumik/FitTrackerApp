package com.example.fittrackerapp.daoInterfaces

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fittrackerapp.entities.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutDao {

    @Insert(entity = Workout::class)
    suspend fun insert(workout: Workout)

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getById(workoutId: Int): Workout?

    @Delete
    suspend fun delete(workout: Workout)

    @Update
    suspend fun update(workout: Workout)

    @Query("SELECT * FROM workouts")
    suspend fun getAll(): Flow<List<Workout>>
}