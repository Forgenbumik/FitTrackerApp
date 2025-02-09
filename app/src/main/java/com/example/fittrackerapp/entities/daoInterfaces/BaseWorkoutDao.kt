package com.example.fittrackerapp.entities.daoInterfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fittrackerapp.entities.BaseWorkout

@Dao
interface BaseWorkoutDao {

    @Insert(entity = BaseWorkout::class)
    suspend fun insert(workout: BaseWorkout)

    @Query("SELECT * FROM base_workouts WHERE id = :workoutId")
    suspend fun getById(workoutId: Int): BaseWorkout?

    @Delete
    suspend fun delete(workout: BaseWorkout)

    @Update
    suspend fun update(workout: BaseWorkout)

    @Query("SELECT * FROM base_workouts")
    suspend fun getAll(): List<BaseWorkout>
}