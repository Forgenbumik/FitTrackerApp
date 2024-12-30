package com.example.fittrackerapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "completed_exercise_details",
    foreignKeys = [
        ForeignKey(
            entity = CompletedWorkout::class,
            parentColumns = ["id"],
            childColumns = ["completed_workout_id"]
        ),
        ForeignKey(
            entity = CompletedExercise::class,
            parentColumns = ["id"],
            childColumns = ["completed_exercise_id"]
        )
    ]
)
data class CompletedWorkoutDetail(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "completed_workout_id") val completedWorkoutId: Int,
    @ColumnInfo(name = "completed_exercise_id") val completedExerciseId: Int
)