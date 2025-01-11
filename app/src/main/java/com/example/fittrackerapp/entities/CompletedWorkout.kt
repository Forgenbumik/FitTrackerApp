package com.example.fittrackerapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout

@Entity(
    tableName = "completed_workouts",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class CompletedWorkout(
    @PrimaryKey override val id: Int,
    @ColumnInfo(name = "workout_id") val workoutId: Int,
    @ColumnInfo(name = "duration") override val duration: Int,
    @ColumnInfo(name = "notes") override val notes: String
): BaseCompletedWorkout()