package com.example.fittrackerapp.daoInterfaces

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fittrackerapp.entities.CompletedExerciseDetail

interface CompletedExerciseDetailDao {

    @Insert(entity = CompletedExerciseDetail::class)
    suspend fun insertNewCompletedExerciseDetail(completedExerciseDetail: CompletedExerciseDetail)

    @Query("SELECT * FROM completed_exercise_details WHERE id = :completedExerciseDetailId")
    suspend fun getCompletedExerciseDetailById(completedExerciseDetailId: Int): CompletedExerciseDetail?

    @Delete
    suspend fun deleteCompletedExerciseDetail(completedExerciseDetail: CompletedExerciseDetail)

}