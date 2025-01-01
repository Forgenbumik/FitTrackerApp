package com.example.fittrackerapp.daoInterfaces

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fittrackerapp.entities.CompletedWorkoutDetail

interface CompletedWorkoutDetailDao {
    @Insert(entity = CompletedWorkoutDetail::class)
    suspend fun insertNewCompletedWorkoutDetail(completedWorkoutDetail: CompletedWorkoutDetail)

    @Query("SELECT * FROM completed_workout_details WHERE id = :completedWorkoutDetailId")
    suspend fun getCompletedWorkoutDetailById(completedWorkoutDetailId: Int): CompletedWorkoutDetail?

    @Delete
    suspend fun deleteCompletedWorkoutDetail(completedWorkoutDetail: CompletedWorkoutDetail)
}