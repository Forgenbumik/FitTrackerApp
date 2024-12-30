package com.example.fittrackerapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed_workout_details")
data class CompletedWorkoutDetail(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "completed_workout_id") val completedWorkoutId: Int,
    @ColumnInfo(name = "completed_exercise_id") val completedExerciseId: Int
)