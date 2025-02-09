package com.example.fittrackerapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "base_completed_workout")
data class BaseCompletedWorkout(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "base_workout_id") val baseWorkoutId: Int,
    @ColumnInfo val duration: Int,
    @ColumnInfo val notes: String,
    @ColumnInfo(name = "begin_time") val beginTime: String
)