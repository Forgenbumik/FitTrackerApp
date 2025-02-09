package com.example.fittrackerapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "favourite_workouts",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onUpdate = ForeignKey.CASCADE
        )
    ])
data class FavouriteWorkout(
    @PrimaryKey(autoGenerate = false) val favouriteWorkoutPosition: Int,
    @ColumnInfo(name="workout_id") val workoutId: Int
)