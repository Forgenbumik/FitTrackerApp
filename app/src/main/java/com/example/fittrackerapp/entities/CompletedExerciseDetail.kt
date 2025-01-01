package com.example.fittrackerapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "completed_exercise_details",
    foreignKeys = [
        ForeignKey(
            entity = CompletedExercise::class,
            parentColumns = ["id"],
            childColumns = ["completed_exercise_id"]
        ),
        ForeignKey(
            entity = Set::class,
            parentColumns = ["id"],
            childColumns = ["set_id"]
        )
    ]
)
data class CompletedExerciseDetail(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "completed_exercise_id") val completedExerciseId: Int,
    @ColumnInfo(name = "set_id") val setId: Int
)