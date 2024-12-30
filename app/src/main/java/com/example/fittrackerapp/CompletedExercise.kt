package com.example.fittrackerapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "completed_exercises")
data class CompletedExercise(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "exercise_id") val exerciseId: Int,
    @ColumnInfo(name = "duration") val duration: Int,
    @ColumnInfo(name = "notes") val notes: String
)