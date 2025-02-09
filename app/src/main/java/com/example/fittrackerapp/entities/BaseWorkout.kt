package com.example.fittrackerapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "base_workouts")
class BaseWorkout(
    @PrimaryKey val id: Int,
    @ColumnInfo val name: String,
    @ColumnInfo val type: String,
    @ColumnInfo(name = "concrete_workout_id") val concreteWorkoutId: Int,
    @ColumnInfo(name = "is_user_defined") val isUserDefined: Boolean,
    @ColumnInfo(name = "is_using") val isUsing: Boolean,
    @ColumnInfo(name = "last_user_date") val lastUsedDate: String
)