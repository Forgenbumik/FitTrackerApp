package com.example.fittrackerapp.daoInterfaces

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fittrackerapp.entities.Category
import com.example.fittrackerapp.entities.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseDao {

    @Insert(entity = Exercise::class)
    suspend fun insert(exercise: Exercise)

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getById(exerciseId: Int): Exercise?

    @Delete
    suspend fun delete(exercise: Exercise)

    @Update
    suspend fun update(exercise: Exercise)

    @Query("SELECT * FROM exercises")
    suspend fun getAll(): Flow<List<Exercise>>
}