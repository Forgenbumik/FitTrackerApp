package com.example.fittrackerapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout

@Entity(
    tableName = "completed_exercises",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CompletedWorkout::class,
            parentColumns = ["id"],
            childColumns = ["completed_workout_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)

data class CompletedExercise(
    @PrimaryKey override val id: Int,
    @ColumnInfo(name = "exercise_id") val exerciseId: Int,
    @ColumnInfo(name = "completed_workout_id") val completedWorkoutId: Long,
    @ColumnInfo(name = "duration") override val duration: Int,
    @ColumnInfo(name = "notes") override val notes: String
) : BaseCompletedWorkout()