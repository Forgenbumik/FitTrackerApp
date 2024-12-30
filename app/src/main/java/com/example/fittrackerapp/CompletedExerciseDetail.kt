package com.example.fittrackerapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed_exercise_details")
data class CompletedExerciseDetail(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "completed_exercise_id") val completedExerciseId: Int,
    @ColumnInfo(name = "set_id") val setId: Int
)