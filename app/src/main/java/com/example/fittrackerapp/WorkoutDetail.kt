package com.example.fittrackerapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "workout_details")
data class WorkoutDetail(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "workout_id") val workoutId: Int,
    @ColumnInfo(name = "exercise_id") val exerciseId: Int
)