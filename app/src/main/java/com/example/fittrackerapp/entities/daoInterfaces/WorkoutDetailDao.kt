package com.example.fittrackerapp.entities.daoInterfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fittrackerapp.entities.WorkoutDetail

@Dao
interface WorkoutDetailDao {
    @Insert(entity = WorkoutDetail::class)
    suspend fun insert(workoutDetail: WorkoutDetail)

    @Query("SELECT * FROM workout_details WHERE id = :workoutDetailId")
    suspend fun getById(workoutDetailId: Int): WorkoutDetail?

    @Delete
    suspend fun delete(workoutDetail: WorkoutDetail)

    @Update
    suspend fun update(workoutDetail: WorkoutDetail)
}