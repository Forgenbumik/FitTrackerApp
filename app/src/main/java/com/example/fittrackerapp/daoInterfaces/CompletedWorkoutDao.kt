package com.example.fittrackerapp.daoInterfaces

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fittrackerapp.entities.CompletedWorkout

interface CompletedWorkoutDao {
    @Insert(entity = CompletedWorkout::class)
    suspend fun insertNewCompletedWorkout(completedWorkout: CompletedWorkout)

    @Query("SELECT * FROM completed_workouts WHERE id = :completedWorkoutId")
    suspend fun getWorkoutById(completedWorkoutId: Int): CompletedWorkout?

    @Delete
    suspend fun deleteCompletedWorkout(completedWorkout: CompletedWorkout)
}