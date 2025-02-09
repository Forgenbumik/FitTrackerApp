package com.example.fittrackerapp.entities.daoInterfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fittrackerapp.entities.CompletedWorkout
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedWorkoutDao {
    @Insert(entity = CompletedWorkout::class)
    suspend fun insert(completedWorkout: CompletedWorkout)

    @Query("SELECT * FROM completed_workouts WHERE id = :completedWorkoutId")
    suspend fun getById(completedWorkoutId: Int): CompletedWorkout?

    @Delete
    suspend fun delete(completedWorkout: CompletedWorkout)

    @Update
    suspend fun update(completedWorkout: CompletedWorkout)
}