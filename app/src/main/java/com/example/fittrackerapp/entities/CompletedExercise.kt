package com.example.fittrackerapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "completed_exercises",
    foreignKeys = [
        ForeignKey(
            entity = BaseWorkout::class,
            parentColumns = ["id"],
            childColumns = ["base_workout_id"],
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BaseCompletedWorkout::class,
            parentColumns = ["id"],
            childColumns = ["base_completed_workout_id"],
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
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "base_completed_workout_id") val workoutId: Int,
    @ColumnInfo(name = "completed_workout_id") val completedWorkoutId: Long
): BaseCompletedWorkoutClass() {

}