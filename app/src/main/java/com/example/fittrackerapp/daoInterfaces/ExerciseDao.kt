package com.example.fittrackerapp.daoInterfaces

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fittrackerapp.entities.Exercise

interface ExerciseDao {

    @Insert(entity = Exercise::class)
    suspend fun insertNewExercise(exercise: Exercise)

    @Query("SELECT * FROM sets WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: Int): Exercise?

    @Delete
    suspend fun deleteExercise(exercise: Exercise)
}