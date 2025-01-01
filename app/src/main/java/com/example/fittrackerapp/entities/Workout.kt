package com.example.fittrackerapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fittrackerapp.abstractclasses.BaseWorkout

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey override val id: Int,
    @ColumnInfo(name = "name") override val name: String
): BaseWorkout()