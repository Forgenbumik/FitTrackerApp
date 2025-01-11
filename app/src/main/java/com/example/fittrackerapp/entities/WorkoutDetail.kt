package com.example.fittrackerapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_details",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutDetail(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "workout_id") val workoutId: Int,
    @ColumnInfo(name = "exercise_id") val exerciseId: Int
)