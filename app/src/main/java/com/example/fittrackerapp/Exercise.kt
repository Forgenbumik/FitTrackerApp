package com.example.fittrackerapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String
)