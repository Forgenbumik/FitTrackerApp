package com.example.fittrackerapp.daoInterfaces

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fittrackerapp.entities.Category
import com.example.fittrackerapp.entities.CompletedWorkout
import com.example.fittrackerapp.entities.Workout
import kotlinx.coroutines.flow.Flow

interface CompletedWorkoutDao {
    @Insert(entity = CompletedWorkout::class)
    suspend fun insert(completedWorkout: CompletedWorkout)

    @Query("SELECT * FROM completed_workouts WHERE id = :completedWorkoutId")
    suspend fun getById(completedWorkoutId: Int): CompletedWorkout?

    @Delete
    suspend fun delete(completedWorkout: CompletedWorkout)

    @Update
    suspend fun update(completedWorkout: CompletedWorkout)

    @Query("SELECT * FROM completed_workouts")
    suspend fun getAll(): Flow<List<CompletedWorkout>>
}