package com.example.fittrackerapp.daoInterfaces

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fittrackerapp.entities.WorkoutDetail

interface WorkoutDetailDao {
    @Insert(entity = WorkoutDetail::class)
    suspend fun insertNewWorkoutDetail(workoutDetail: WorkoutDetail)

    @Query("SELECT * FROM workout_details WHERE id = :workoutDetailId")
    suspend fun getWorkoutDetailById(workoutDetailId: Int): WorkoutDetail?

    @Delete
    suspend fun deleteWorkoutDetail(workoutDetail: WorkoutDetail)
}