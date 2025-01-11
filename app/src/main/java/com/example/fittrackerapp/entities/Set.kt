package com.example.fittrackerapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlin.collections.Set

@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = CompletedExercise::class,
            parentColumns = ["id"],
            childColumns = ["completed_exercise_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class Set(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "completed_exercise_id") val completedExerciseId: Long,
    @ColumnInfo(name = "duration") val duration: Int,
    @ColumnInfo(name = "reps") val reps: Int,
    @ColumnInfo(name = "weight") val weight: Double
)